"""
延迟分账与多次分账示例：altHandle.singleLaterAllocate / allocateQuery /
manyLaterAllocate / finishAllocate / altManyOrderQuery / altManyTotalQuery。

依赖：pip install requests cryptography
示例只演示 /allocFunds V1.3.0 协议骨架，请替换所有占位符。
金额字段按文档要求使用字符串；RSA 使用 sign_type=21 + MD5withRSA。
"""

import base64
import hashlib
import json
import random
import string
import time

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


def build_sign_string(sign_params: dict) -> str:
    return "&".join(f"{k}={sign_params[k]}" for k in sorted(sign_params))


def md5_sign(sign_params: dict, merchant_key: str) -> str:
    sign_str = build_sign_string(sign_params)
    return hashlib.md5((sign_str + "&key=" + merchant_key).encode("utf-8")).hexdigest().upper()


def rsa_sign_md5(sign_params: dict, private_key_pem: bytes) -> str:
    sign_str = build_sign_string(sign_params)
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


def single_later_allocate(mch_order_no: str, alt_mch_no: str) -> str:
    data = {
        "mch_order_no": mch_order_no,
        "alt_order_no": "SGL" + str(int(time.time())) + random_alpha_num(6),
        "alt_info": [
            {
                "alt_mch_no": alt_mch_no,
                "alt_amount": "6.00",
            }
        ],
        "callback_url": CALLBACK_URL,
    }
    return request_alloc_funds("altHandle.singleLaterAllocate", "1.1", data)


def query_delay_allocate(alt_order_no: str) -> str:
    return request_alloc_funds(
        "altHandle.allocateQuery",
        "1.1",
        {"alt_order_no": alt_order_no},
    )


def many_later_allocate(mch_order_no: str, alt_mch_no: str) -> str:
    data = {
        "mch_order_no": mch_order_no,
        "alt_order_no": "ALT" + str(int(time.time())) + random_alpha_num(6),
        "alt_this_amount": "10.00",
        "alt_this_marketing_amount": "0.00",
        "alt_info": [
            {
                "alt_mch_no": alt_mch_no,
                "alt_amount": "6.00",
            }
        ],
        "callback_url": CALLBACK_URL,
    }
    return request_alloc_funds("altHandle.manyLaterAllocate", "1.1", data)


def finish_allocate(mch_order_no: str) -> str:
    data = {
        "mch_order_no": mch_order_no,
        "alt_order_no": "FIN" + str(int(time.time())) + random_alpha_num(6),
        "callback_url": CALLBACK_URL,
    }
    return request_alloc_funds("altHandle.finishAllocate", "1.1", data)


def query_single_allocate(alt_order_no: str) -> str:
    return request_alloc_funds(
        "altHandle.altManyOrderQuery",
        "1.1",
        {"alt_order_no": alt_order_no},
    )


def query_total_allocate(mch_order_no: str) -> str:
    return request_alloc_funds(
        "altHandle.altManyTotalQuery",
        "1.1",
        {"mch_order_no": mch_order_no},
    )


if __name__ == "__main__":
    paid_order_no = "PAID_ORDER_NO_PLACEHOLDER"
    alt_mch_no = "ALT_MCH_NO_PLACEHOLDER"
    alt_order_no = "ALT_ORDER_NO_PLACEHOLDER"

    print(single_later_allocate(paid_order_no, alt_mch_no))
    print(query_delay_allocate(alt_order_no))
    print(many_later_allocate(paid_order_no, alt_mch_no))
    print(finish_allocate(paid_order_no))
    print(query_single_allocate(alt_order_no))
    print(query_total_allocate(paid_order_no))
