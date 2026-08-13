"""
分账方入网与结算示例：altmch.create / altSettle.launch / altAccount.get。
依赖：pip install requests cryptography
示例只演示 /allocFunds 协议骨架，请替换所有占位符。
V1.4.2 分账方添加无 AES 加密；RSA 使用 sign_type=21 + MD5withRSA。
"""

import base64
import hashlib
import json
import random
import string
import time
from decimal import Decimal

import requests
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding


URL = "https://www.joinpay.com/allocFunds"
MERCHANT_NO = "YOUR_MERCHANT_NO"
MERCHANT_KEY = "YOUR_MERCHANT_KEY"
CALLBACK_URL = "YOUR_CALLBACK_URL"
SIGN_TYPE = "1"  # 1=MD5，21=RSA(MD5withRSA)
MERCHANT_PRIVATE_KEY_PEM = b"""-----BEGIN PRIVATE KEY-----
...
-----END PRIVATE KEY-----"""
JP_PUBLIC_KEY_PEM = b"""-----BEGIN PUBLIC KEY-----
...
-----END PUBLIC KEY-----"""


def random_alpha_num(length: int) -> str:
    chars = string.ascii_letters + string.digits
    return "".join(random.choice(chars) for _ in range(length))


def compact_json(data: dict) -> str:
    return json.dumps(data, separators=(",", ":"), ensure_ascii=False)


def md5_sign(sign_params: dict, merchant_key: str) -> str:
    sign_str = "&".join(f"{k}={sign_params[k]}" for k in sorted(sign_params))
    return hashlib.md5((sign_str + "&key=" + merchant_key).encode("utf-8")).hexdigest().upper()


def hex_text(text: str) -> str:
    return text.encode("utf-8").hex().upper()


def rsa_sign_md5(sign_params: dict, private_key_pem: bytes) -> str:
    sign_str = "&".join(f"{k}={sign_params[k]}" for k in sorted(sign_params))
    private_key = serialization.load_pem_private_key(private_key_pem, password=None)
    signature = private_key.sign(sign_str.encode("utf-8"), padding.PKCS1v15(), hashes.MD5())
    return base64.b64encode(signature).decode("utf-8")


def rsa_verify_md5(data: str, public_key_pem: bytes, sign_value: str) -> bool:
    public_key = serialization.load_pem_public_key(public_key_pem)
    signature = base64.b64decode(bytes.fromhex(sign_value).decode("utf-8"))
    public_key.verify(signature, data.encode("utf-8"), padding.PKCS1v15(), hashes.MD5())
    return True


def sign(sign_params: dict) -> str:
    if SIGN_TYPE == "1":
        return md5_sign(sign_params, MERCHANT_KEY)
    if SIGN_TYPE == "21":
        return rsa_sign_md5(sign_params, MERCHANT_PRIVATE_KEY_PEM)
    raise ValueError("SIGN_TYPE must be 1 or 21")


def request_alloc_funds(method: str, version: str, data: dict) -> str:
    data_json = compact_json(data)
    sign_params = {
        "data": data_json,
        "mch_no": MERCHANT_NO,
        "method": method,
        "rand_str": random_alpha_num(32),
        "sign_type": SIGN_TYPE,
        "version": version,
    }
    request_body = {
        "data": data,
        "mch_no": MERCHANT_NO,
        "method": method,
        "rand_str": sign_params["rand_str"],
        "sign_type": SIGN_TYPE,
        "version": version,
        "sign": sign(sign_params),
    }
    response = requests.post(URL, json=request_body, timeout=30)
    response.raise_for_status()
    return response.text


def create_alt_mch() -> str:
    alt_mch_name = "示例分账方名称"
    data = {
        "login_name": "alt_demo_" + str(int(time.time())),
        "alt_mch_name": alt_mch_name,
        "alt_mch_short_name": "示例分账方",
        "alt_merchant_type": "12",
        "phone_no": "13800000000",
        "legal_person": "示例法人",
        "id_card_no": "110101199001011234",
        "id_card_expiry": "2099-12-31",
        "busi_contact_name": "示例联系人",
        "busi_contact_mobile_no": "13800000000",
        "license_no": "91440101MA00000000",
        "license_expiry": "2099-12-31",
        "manage_scope": "信息技术服务",
        "manage_addr": "广州市示例路1号",
        "sett_mode": "2",
        "sett_date_type": "2",
        "risk_day": "1",
        "bank_account_type": "4",
        "bank_account_name": alt_mch_name,
        "bank_account_no": "6222000000000000000",
        "bank_channel_no": "BANK_CHANNEL_NO_PLACEHOLDER",
        "notify_url": CALLBACK_URL,
    }
    data_json = compact_json(data)
    sign_params = {
        "data": data_json,
        "mch_no": MERCHANT_NO,
        "method": "altmch.create",
        "rand_str": random_alpha_num(32),
        "sign_type": SIGN_TYPE,
        "version": "1.1",
    }
    request_body = {
        "data": data,
        "mch_no": MERCHANT_NO,
        "method": "altmch.create",
        "rand_str": sign_params["rand_str"],
        "sign_type": SIGN_TYPE,
        "version": "1.1",
        "sign": sign(sign_params),
    }
    response = requests.post(URL, json=request_body, timeout=30)
    response.raise_for_status()
    return response.text


def launch_settle(alt_mch_no: str) -> str:
    settle_amount = Decimal("10.00")
    data = {
        "alt_mch_no": alt_mch_no,
        "product_code": "2",
        "settle_amount": f"{settle_amount:.2f}",
        "settle_fee": "0.00",
        "mch_order_no": "ST" + str(int(time.time())) + random_alpha_num(6),
        "callback_url": CALLBACK_URL,
    }
    return request_alloc_funds("altSettle.launch", "1.1", data)


def query_account(alt_mch_no: str) -> str:
    return request_alloc_funds("altAccount.get", "1.1", {"alt_mch_no": alt_mch_no})


if __name__ == "__main__":
    print(create_alt_mch())
    print(launch_settle("ALT_MCH_NO_PLACEHOLDER"))
    print(query_account("ALT_MCH_NO_PLACEHOLDER"))
