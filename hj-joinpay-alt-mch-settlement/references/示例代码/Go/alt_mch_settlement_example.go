package main

import (
	"bytes"
	"crypto"
	"crypto/md5"
	"crypto/rand"
	"crypto/rsa"
	"crypto/x509"
	"encoding/base64"
	"encoding/hex"
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
	apiURL        = "https://www.joinpay.com/allocFunds"
	merchantNo    = "YOUR_MERCHANT_NO"
	merchantKey   = "YOUR_MERCHANT_KEY"
	callbackURL   = "YOUR_CALLBACK_URL"
	signType      = "1" // 1=MD5，21=RSA(MD5withRSA)
	privateKeyPEM = `-----BEGIN PRIVATE KEY-----
...
-----END PRIVATE KEY-----`
	jpPublicKeyPEM = `-----BEGIN PUBLIC KEY-----
...
-----END PUBLIC KEY-----`
)

func main() {
	fmt.Println(createAltMch())
	fmt.Println(launchSettle("ALT_MCH_NO_PLACEHOLDER"))
	fmt.Println(queryAccount("ALT_MCH_NO_PLACEHOLDER"))
}

func createAltMch() string {
	altMchName := "示例分账方名称"
	data := map[string]interface{}{
		"login_name":             "alt_demo_" + fmt.Sprint(time.Now().Unix()),
		"alt_mch_name":           altMchName,
		"alt_mch_short_name":     "示例分账方",
		"alt_merchant_type":      "12",
		"phone_no":               "13800000000",
		"legal_person":           "示例法人",
		"id_card_no":             "110101199001011234",
		"id_card_expiry":         "2099-12-31",
		"busi_contact_name":      "示例联系人",
		"busi_contact_mobile_no": "13800000000",
		"license_no":             "91440101MA00000000",
		"license_expiry":         "2099-12-31",
		"manage_scope":           "信息技术服务",
		"manage_addr":            "广州市示例路1号",
		"sett_mode":              "2",
		"sett_date_type":         "2",
		"risk_day":               "1",
		"bank_account_type":      "4",
		"bank_account_name":      altMchName,
		"bank_account_no":        "6222000000000000000",
		"bank_channel_no":        "BANK_CHANNEL_NO_PLACEHOLDER",
		"notify_url":             callbackURL,
	}
	return requestAllocFunds("altmch.create", "1.1", data, "")
}

func launchSettle(altMchNo string) string {
	data := map[string]interface{}{
		"alt_mch_no":    altMchNo,
		"product_code":  "2",
		"settle_amount": "10.00",
		"settle_fee":    "0.00",
		"mch_order_no":  "ST" + fmt.Sprint(time.Now().Unix()) + randomAlphaNum(6),
		"callback_url":  callbackURL,
	}
	return requestAllocFunds("altSettle.launch", "1.1", data, "")
}

func queryAccount(altMchNo string) string {
	return requestAllocFunds("altAccount.get", "1.1", map[string]interface{}{"alt_mch_no": altMchNo}, "")
}

func requestAllocFunds(method, version string, data map[string]interface{}, aesKey string) string {
	dataBytes, err := json.Marshal(data)
	if err != nil {
		panic(err)
	}
	signParams := map[string]string{
		"data":      string(dataBytes),
		"mch_no":    merchantNo,
		"method":    method,
		"rand_str":  randomAlphaNum(32),
		"sign_type": signType,
		"version":   version,
	}
	requestBody := map[string]interface{}{
		"data":      data,
		"mch_no":    merchantNo,
		"method":    method,
		"rand_str":  signParams["rand_str"],
		"sign_type": signType,
		"version":   version,
		"sign":      sign(signParams),
	}
	if aesKey != "" {
		requestBody["aes_key"] = aesKey
	}

	bodyBytes, _ := json.Marshal(requestBody)
	resp, err := http.Post(apiURL, "application/json;charset=UTF-8", bytes.NewReader(bodyBytes))
	if err != nil {
		panic(err)
	}
	defer resp.Body.Close()
	respBytes, _ := io.ReadAll(resp.Body)
	return string(respBytes)
}

func md5Sign(params map[string]string) string {
	signStr := sortedQuery(params) + "&key=" + merchantKey
	sum := md5.Sum([]byte(signStr))
	return strings.ToUpper(hex.EncodeToString(sum[:]))
}

func sign(params map[string]string) string {
	if signType == "1" {
		return md5Sign(params)
	}
	if signType == "21" {
		return rsaSignMD5(params)
	}
	panic("signType must be 1 or 21")
}

func rsaSignMD5(params map[string]string) string {
	privateKey := parsePrivateKey(privateKeyPEM)
	hashed := md5.Sum([]byte(sortedQuery(params)))
	signature, err := rsa.SignPKCS1v15(rand.Reader, privateKey, crypto.MD5, hashed[:])
	if err != nil {
		panic(err)
	}
	return base64.StdEncoding.EncodeToString(signature)
}

func rsaVerifyMD5(data, publicPEM, signValue string) bool {
	publicKey := parsePublicKey(publicPEM)
	base64TextBytes, err := hex.DecodeString(signValue)
	if err != nil {
		panic(err)
	}
	signature, err := base64.StdEncoding.DecodeString(string(base64TextBytes))
	if err != nil {
		panic(err)
	}
	hashed := md5.Sum([]byte(data))
	return rsa.VerifyPKCS1v15(publicKey, crypto.MD5, hashed[:], signature) == nil
}

func hexText(text string) string {
	return strings.ToUpper(hex.EncodeToString([]byte(text)))
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

func randomAlphaNum(length int) string {
	const chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
	var out strings.Builder
	for i := 0; i < length; i++ {
		n, _ := rand.Int(rand.Reader, big.NewInt(int64(len(chars))))
		out.WriteByte(chars[n.Int64()])
	}
	return out.String()
}
