package Java.SpringBoot3.enumeration;

/*
Why two different types?
    The idea is that verification can happen in different contexts, and each context needs to be handled differently:
(1) ACCOUNT (new account verification)
    Used when a user signs up for the first time.
    The system sends a verification link (usually via email) to confirm the user’s identity and activate the account.
    Example: https://yourapp.com/verify?type=ACCOUNT&token=abc123
(2) PASSWORD (password reset verification)
    Used when a user forgets their password or requests a reset.
    The system sends a different kind of link to verify the request before allowing the user to set a new password.
    Example: https://yourapp.com/verify?type=PASSWORD&token=xyz789

Why not just one type?
(1) Security reasons: Account activation and password reset are two distinct flows. Mixing them could lead to confusion or vulnerabilities.
(2) Business logic separation: Each type triggers different backend logic:
        ACCOUNT → enable the user’s account.
        PASSWORD → allow password change.
(3) Clarity in code: Using an enum makes the intent explicit. Developers can easily see which verification flow is being executed.
 */
public enum VerificationType {
    //we have two different types of URL either a new account or a password
    ACCOUNT("ACCOUNT"),
    PASSWORD("PASSWORD");

    private final String type; //每個列舉常量都會綁定一個字串值，例如 "ACCOUNT" 或 "PASSWORD"。

    VerificationType(String type) { this.type = type; }
    // VerificationType(String type) : 列舉的建構子，用來初始化 type 欄位。
    // 回傳 type.toLowerCase() → 把字串轉成小寫。
    // 例如：VerificationType.ACCOUNT.getType() → "account"
    //      VerificationType.PASSWORD.getType() → "password"

    public String getType() {
        return this.type.toLowerCase();
        /*
        📖 解釋 :
            (1) public String getType()
                    這是一個公開方法，回傳型別是 String。
                    任何其他類別都可以呼叫這個方法來取得 VerificationType 的字串表示。
            (2) this.type
                    type 是在 VerificationType 列舉裡定義的私有欄位：
                        java
                        private final String type;
                    每個列舉常量（例如 ACCOUNT("ACCOUNT")、PASSWORD("PASSWORD")）都會把字串 "ACCOUNT" 或 "PASSWORD" 存到這個欄位。
         */
    }
}
