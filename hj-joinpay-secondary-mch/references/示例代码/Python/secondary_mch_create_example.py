"""
二级商户新增示例：secondaryMch.create。
依赖：pip install cryptography requests
示例只演示协议骨架，请替换所有占位符。
"""

import base64
import json
import random
import string
import time

import requests
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes


URL = "https://api.joinpay.com/altFunds"
MERCHANT_NO = "YOUR_MERCHANT_NO"
CALLBACK_URL = "YOUR_CALLBACK_URL"
MERCHANT_PRIVATE_KEY_PEM = b"""-----BEGIN PRIVATE KEY-----
...
-----END PRIVATE KEY-----"""
PLATFORM_PUBLIC_KEY_PEM = b"""-----BEGIN PUBLIC KEY-----
...
-----END PUBLIC KEY-----"""


def random_alpha_num(length: int) -> str:
    chars = string.ascii_letters + string.digits
    return "".join(random.choice(chars) for _ in range(length))


def pkcs7_pad(data: bytes, block_size: int = 16) -> bytes:
    pad_len = block_size - len(data) % block_size
    return data + bytes([pad_len]) * pad_len


def aes_encrypt(plain_text: str, aes_key: str) -> str:
    cipher = Cipher(algorithms.AES(aes_key.encode("utf-8")), modes.ECB())
    encryptor = cipher.encryptor()
    encrypted = encryptor.update(pkcs7_pad(plain_text.encode("utf-8"))) + encryptor.finalize()
    return base64.b64encode(encrypted).decode("utf-8")


def rsa_sign_md5(data: str, private_key_pem: bytes) -> str:
    private_key = serialization.load_pem_private_key(private_key_pem, password=None)
    signature = private_key.sign(data.encode("utf-8"), padding.PKCS1v15(), hashes.MD5())
    return base64.b64encode(signature).decode("utf-8")


def rsa_encrypt_public(data: str, public_key_pem: bytes) -> str:
    public_key = serialization.load_pem_public_key(public_key_pem)
    encrypted = public_key.encrypt(data.encode("utf-8"), padding.PKCS1v15())
    return base64.b64encode(encrypted).decode("utf-8")


def main():
    aes_key = random_alpha_num(16)
    method = "secondaryMch.create"
    version = "1.0"
    rand_str = random_alpha_num(32)

    data = {
        "mch_order_no": f"SM{int(time.time())}{random_alpha_num(6)}",
        "callback_url": CALLBACK_URL,
        "mch_info": {
            "login_name": f"demo_login_{int(time.time())}",
            "alt_mch_name": "示例商户名称",
            "alt_merchant_type": "12",
            "busi_contact_name": aes_encrypt("示例联系人", aes_key),
            "busi_contact_mobile_no": aes_encrypt("13800000000", aes_key),
            "bussiness_type": "JP3041",
            "province": "5800",
            "city": "5810",
            "county": "5817",
            "manage_scope": "示例经营范围",
            "manage_addr": "示例经营地址",
        },
        "id_card_info": {
            "legal_person": aes_encrypt("示例法人", aes_key),
            "phone_no": aes_encrypt("13800000000", aes_key),
            "id_card_no": aes_encrypt("110101199001011234", aes_key),
            "id_card_valid_time_begin": "2020-01-01",
            "id_card_valid_time_end": "2099-12-31",
            "id_doc_copy": "IMAGE_ID_FACE",
            "id_card_national": "IMAGE_ID_BACK",
            "beneficiary_owner": "1",
        },
        "license_info": {
            "license_no": "LICENSE_NO_PLACEHOLDER",
            "license_expiry": "2099-12-31",
            "trade_licence": "IMAGE_ID_LICENSE",
        },
        "account_type": {
            "sett_mode": "2",
            "sett_date_type": "1",
            "risk_day": "1",
        },
        "account_info": {
            "bank_account_type": "4",
            "bank_account_name": aes_encrypt("示例商户名称", aes_key),
            "bank_account_no": aes_encrypt("6222000000000000000", aes_key),
            "bank_channel_no": "BANK_CHANNEL_NO",
            "open_account_licence": "IMAGE_ID_BANK",
        },
        "sales_info": [{
            "material_id": "locallife_proof",
            "material_cont": [
                {"material_type": "door_photo", "material_info": "IMAGE_ID_DOOR"},
                {"material_type": "environment_photo", "material_info": "IMAGE_ID_ENV"},
                {"material_type": "co_agreement", "material_info": "IMAGE_ID_AGREEMENT"},
            ],
        }],
    }

    data_json = json.dumps(data, separators=(",", ":"), ensure_ascii=False)
    sign_params = {
        "data": data_json,
        "mch_no": MERCHANT_NO,
        "method": method,
        "rand_str": rand_str,
        "sign_type": "2",
        "version": version,
    }
    sign_str = "&".join(f"{k}={sign_params[k]}" for k in sorted(sign_params))

    request_body = dict(sign_params)
    request_body["sign"] = rsa_sign_md5(sign_str, MERCHANT_PRIVATE_KEY_PEM)
    request_body["sec_key"] = rsa_encrypt_public(aes_key, PLATFORM_PUBLIC_KEY_PEM)

    response = requests.post(URL, json=request_body, timeout=30)
    print(response.text)


if __name__ == "__main__":
    main()
