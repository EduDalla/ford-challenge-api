"""Emite JWT de fixture para o Compose local; nao autentica usuarios reais."""

import argparse
import base64
import hashlib
import hmac
import json
import time


def encode(value):
    return base64.urlsafe_b64encode(value).rstrip(b"=")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("perfil", choices=["ADMIN", "GESTOR", "ANALISTA"])
    args = parser.parse_args()
    identity = {"ADMIN": "1", "GESTOR": "2", "ANALISTA": "3"}[args.perfil]
    now = int(time.time())
    payload = {
        "sub": "00000000-0000-4000-8000-00000000000" + identity,
        "iss": "http://pulso-local/auth",
        "aud": "authenticated",
        "iat": now,
        "exp": now + 3600,
    }
    header = {"alg": "HS256", "typ": "JWT"}
    signing_input = b".".join(
        encode(json.dumps(value, separators=(",", ":")).encode())
        for value in (header, payload)
    )
    signature = hmac.new(
        b"local-user-signing-secret-only-for-development", signing_input, hashlib.sha256
    ).digest()
    print((signing_input + b"." + encode(signature)).decode())


if __name__ == "__main__":
    main()
