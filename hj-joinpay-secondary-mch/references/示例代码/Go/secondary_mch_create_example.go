package main

import (
	"bytes"
	"crypto"
	"crypto/aes"
	"crypto/md5"
	"crypto/rand"
	"crypto/rsa"
	"crypto/x509"
	"encoding/base64"
	"encoding/json"
	"encoding/pem"
	"fmt"
	"io"
	"math/big"
	"net/http"
	"sort"
	"strings"
	"time"
)

const (
	apiURL        = "https://api.joinpay.com/altFunds"
	merchantNo    = "YOUR_MERCHANT_NO"
	callbackURL   = "YOUR_CALLBACK_URL"
	privateKeyPEM = `-----BEGIN PRIVATE KEY-----
...
-----END PRIVATE KEY-----`
	platformPublicKeyPEM = `-----BEGIN PUBLIC KEY-----
...
-----END PUBLIC KEY-----`
)

func main() {
	aesKey := randomAlphaNum(16)
	method := "secondaryMch.create"
	version := "1.0"
	randStr := randomAlphaNum(32)

	data := map[string]interface{}{
		"mch_order_no": "SM" + fmt.Sprint(time.Now().Unix()) + randomAlphaNum(6),
		"callback_url": callbackURL,
		"mch_info": map[string]interface{}{
			"login_name":             "demo_login_" + fmt.Sprint(time.Now().Unix()),
			"alt_mch_name":           "示例商户名称",
			"alt_merchant_type":      "12",
			"busi_contact_name":      aesEncrypt("示例联系人", aesKey),
			"busi_contact_mobile_no": aesEncrypt("13800000000", aesKey),
			"bussiness_type":         "JP3041",
			"province":               "5800",
			"city":                   "5810",
			"county":                 "5817",
			"manage_scope":           "示例经营范围",
			"manage_addr":            "示例经营地址",
		},
		"id_card_info": map[string]interface{}{
			"legal_person":             aesEncrypt("示例法人", aesKey),
			"phone_no":                 aesEncrypt("13800000000", aesKey),
			"id_card_no":               aesEncrypt("110101199001011234", aesKey),
			"id_card_valid_time_begin": "2020-01-01",
			"id_card_valid_time_end":   "2099-12-31",
			"id_doc_copy":              "IMAGE_ID_FACE",
			"id_card_national":         "IMAGE_ID_BACK",
			"beneficiary_owner":        "1",
		},
		"license_info": map[string]interface{}{
			"license_no":     "LICENSE_NO_PLACEHOLDER",
			"license_expiry": "2099-12-31",
			"trade_licence":  "IMAGE_ID_LICENSE",
		},
		"account_type": map[string]interface{}{
			"sett_mode":      "2",
			"sett_date_type": "1",
			"risk_day":       "1",
		},
		"account_info": map[string]interface{}{
			"bank_account_type":    "4",
			"bank_account_name":    aesEncrypt("示例商户名称", aesKey),
			"bank_account_no":      aesEncrypt("6222000000000000000", aesKey),
			"bank_channel_no":      "BANK_CHANNEL_NO",
			"open_account_licence": "IMAGE_ID_BANK",
		},
		"sales_info": []interface{}{
			map[string]interface{}{
				"material_id": "locallife_proof",
				"material_cont": []interface{}{
					map[string]interface{}{"material_type": "door_photo", "material_info": "IMAGE_ID_DOOR"},
					map[string]interface{}{"material_type": "environment_photo", "material_info": "IMAGE_ID_ENV"},
					map[string]interface{}{"material_type": "co_agreement", "material_info": "IMAGE_ID_AGREEMENT"},
				},
			},
		},
	}

	dataBytes, _ := json.Marshal(data)
	dataJSON := string(dataBytes)
	signParams := map[string]string{
		"data":      dataJSON,
		"mch_no":    merchantNo,
		"method":    method,
		"rand_str":  randStr,
		"sign_type": "2",
		"version":   version,
	}
	signStr := sortedQuery(signParams)

	requestBody := map[string]string{}
	for k, v := range signParams {
		requestBody[k] = v
	}
	requestBody["sign"] = rsaSignMD5(signStr, privateKeyPEM)
	requestBody["sec_key"] = rsaEncryptPublic(aesKey, platformPublicKeyPEM)

	bodyBytes, _ := json.Marshal(requestBody)
	resp, err := http.Post(apiURL, "application/json;charset=UTF-8", bytes.NewReader(bodyBytes))
	if err != nil {
		panic(err)
	}
	defer resp.Body.Close()
	respBytes, _ := io.ReadAll(resp.Body)
	fmt.Println(string(respBytes))
}

func sortedQuery(params map[string]string) string {
	keys := make([]string, 0, len(params))
	for k := range params {
		keys = append(keys, k)
	}
	sort.Strings(keys)
	parts := make([]string, 0, len(keys))
	for _, k := range keys {
		parts = append(parts, k+"="+params[k])
	}
	return strings.Join(parts, "&")
}

func aesEncrypt(plain, key string) string {
	block, err := aes.NewCipher([]byte(key))
	if err != nil {
		panic(err)
	}
	plainBytes := pkcs7Pad([]byte(plain), block.BlockSize())
	out := make([]byte, len(plainBytes))
	for start := 0; start < len(plainBytes); start += block.BlockSize() {
		block.Encrypt(out[start:start+block.BlockSize()], plainBytes[start:start+block.BlockSize()])
	}
	return base64.StdEncoding.EncodeToString(out)
}

func pkcs7Pad(data []byte, blockSize int) []byte {
	padding := blockSize - len(data)%blockSize
	return append(data, bytes.Repeat([]byte{byte(padding)}, padding)...)
}

func rsaSignMD5(data, privatePEM string) string {
	privateKey := parsePrivateKey(privatePEM)
	sum := md5.Sum([]byte(data))
	signature, err := rsa.SignPKCS1v15(rand.Reader, privateKey, crypto.MD5, sum[:])
	if err != nil {
		panic(err)
	}
	return base64.StdEncoding.EncodeToString(signature)
}

func rsaEncryptPublic(data, publicPEM string) string {
	publicKey := parsePublicKey(publicPEM)
	encrypted, err := rsa.EncryptPKCS1v15(rand.Reader, publicKey, []byte(data))
	if err != nil {
		panic(err)
	}
	return base64.StdEncoding.EncodeToString(encrypted)
}

func parsePrivateKey(privatePEM string) *rsa.PrivateKey {
	block, _ := pem.Decode([]byte(privatePEM))
	if block == nil {
		panic("invalid private key pem")
	}
	key, err := x509.ParsePKCS8PrivateKey(block.Bytes)
	if err != nil {
		panic(err)
	}
	return key.(*rsa.PrivateKey)
}

func parsePublicKey(publicPEM string) *rsa.PublicKey {
	block, _ := pem.Decode([]byte(publicPEM))
	if block == nil {
		panic("invalid public key pem")
	}
	key, err := x509.ParsePKIXPublicKey(block.Bytes)
	if err != nil {
		panic(err)
	}
	return key.(*rsa.PublicKey)
}

func randomAlphaNum(length int) string {
	const chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
	var out strings.Builder
	for i := 0; i < length; i++ {
		n, _ := rand.Int(rand.Reader, big.NewInt(int64(len(chars))))
		out.WriteByte(chars[n.Int64()])
	}
	return out.String()
}
