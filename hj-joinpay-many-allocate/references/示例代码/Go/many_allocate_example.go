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
)

func main() {
	paidOrderNo := "PAID_ORDER_NO_PLACEHOLDER"
	altMchNo := "ALT_MCH_NO_PLACEHOLDER"
	altOrderNo := "ALT_ORDER_NO_PLACEHOLDER"

	fmt.Println(singleLaterAllocate(paidOrderNo, altMchNo))
	fmt.Println(queryDelayAllocate(altOrderNo))
	fmt.Println(manyLaterAllocate(paidOrderNo, altMchNo))
	fmt.Println(finishAllocate(paidOrderNo))
	fmt.Println(querySingleAllocate(altOrderNo))
	fmt.Println(queryTotalAllocate(paidOrderNo))
}

func singleLaterAllocate(mchOrderNo, altMchNo string) string {
	data := map[string]interface{}{
		"mch_order_no": mchOrderNo,
		"alt_order_no": "SGL" + fmt.Sprint(time.Now().UnixMilli()) + randomAlphaNum(6),
		"alt_info": []map[string]string{
			{
				"alt_mch_no": altMchNo,
				"alt_amount": "6.00",
			},
		},
		"callback_url": callbackURL,
	}
	return requestAllocFunds("altHandle.singleLaterAllocate", "1.1", data)
}

func queryDelayAllocate(altOrderNo string) string {
	return requestAllocFunds("altHandle.allocateQuery", "1.1", map[string]interface{}{
		"alt_order_no": altOrderNo,
	})
}

func manyLaterAllocate(mchOrderNo, altMchNo string) string {
	data := map[string]interface{}{
		"mch_order_no":              mchOrderNo,
		"alt_order_no":              "ALT" + fmt.Sprint(time.Now().UnixMilli()) + randomAlphaNum(6),
		"alt_this_amount":           "10.00",
		"alt_this_marketing_amount": "0.00",
		"alt_info": []map[string]string{
			{
				"alt_mch_no": altMchNo,
				"alt_amount": "6.00",
			},
		},
		"callback_url": callbackURL,
	}
	return requestAllocFunds("altHandle.manyLaterAllocate", "1.1", data)
}

func finishAllocate(mchOrderNo string) string {
	data := map[string]interface{}{
		"mch_order_no": mchOrderNo,
		"alt_order_no": "FIN" + fmt.Sprint(time.Now().UnixMilli()) + randomAlphaNum(6),
		"callback_url": callbackURL,
	}
	return requestAllocFunds("altHandle.finishAllocate", "1.1", data)
}

func querySingleAllocate(altOrderNo string) string {
	return requestAllocFunds("altHandle.altManyOrderQuery", "1.1", map[string]interface{}{
		"alt_order_no": altOrderNo,
	})
}

func queryTotalAllocate(mchOrderNo string) string {
	return requestAllocFunds("altHandle.altManyTotalQuery", "1.1", map[string]interface{}{
		"mch_order_no": mchOrderNo,
	})
}

func requestAllocFunds(method, version string, data map[string]interface{}) string {
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
	requestBody := map[string]string{}
	for k, v := range signParams {
		requestBody[k] = v
	}
	requestBody["sign"] = sign(signParams)

	bodyBytes, _ := json.Marshal(requestBody)
	resp, err := http.Post(apiURL, "application/json;charset=UTF-8", bytes.NewReader(bodyBytes))
	if err != nil {
		panic(err)
	}
	defer resp.Body.Close()
	respBytes, _ := io.ReadAll(resp.Body)
	return string(respBytes)
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

func md5Sign(params map[string]string) string {
	signStr := sortedQuery(params) + "&key=" + merchantKey
	sum := md5.Sum([]byte(signStr))
	return strings.ToUpper(hex.EncodeToString(sum[:]))
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
