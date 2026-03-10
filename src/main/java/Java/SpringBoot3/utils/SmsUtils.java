package Java.SpringBoot3.utils;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import static com.twilio.rest.api.v2010.account.Message.creator;

/*
在 Twilio 中，FROM_NUMBER 和 to 不應該相同。
FROM_NUMBER → 你的 Twilio 發送號碼。
to → 接收者的號碼。
 */
public class SmsUtils {
    public static final String FROM_NUMBER = "+1xxxxxxxx";//Your own number from Twilio //FROM_NUMBER 是「發送者 (Sender) 的電話號碼」。
    public static final String SID_KEY = "xxxxxxxxxx"; //Your own key
    public static final String TOKEN_KEY = "xxxxxxxxxx"; //your own key

    public static void sendSMS(String to, String messageBody) {
        Twilio.init(SID_KEY, TOKEN_KEY);//初始化 Twilio API，使用 SID 與 Token 驗證身份。
        Message message = creator(new PhoneNumber("+" + to), new PhoneNumber(FROM_NUMBER), messageBody).create();
        System.out.println(message);
        /*
        creator(...) → 建立一個簡訊物件：
            new PhoneNumber("+" + to) → 收件人號碼。
            new PhoneNumber(FROM_NUMBER) → 發件人號碼。
            messageBody → 短訊內容。
            .create() → 真正發送簡訊。
            System.out.println(message) → 在控制台輸出簡訊物件的資訊（例如 SID、狀態）。
         */
    }

}
