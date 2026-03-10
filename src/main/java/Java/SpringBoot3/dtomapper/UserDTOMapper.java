package Java.SpringBoot3.dtomapper;

import Java.SpringBoot3.domain.Role;
import Java.SpringBoot3.domain.User;
import Java.SpringBoot3.dto.UserDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;


//這是一個 Spring 的註解，表示這個類別會被 Spring 容器管理，並且可以在其他地方被注入使用。
// 在這裡，UserDTOMapper 被標記為一個 Spring Bean。


// 類別 UserDTOMapper
// 這是一個 資料轉換器 (Mapper)，用來在 User 實體 和 UserDTO 資料傳輸物件 之間做轉換。
// 目的：分離 資料庫實體 (domain model) 與 前端/外部 API 傳輸物件 (DTO)，避免直接暴露內部結構。
public class UserDTOMapper {


    // 方法 fromUser(User user)
    // fromUser：把資料庫的 User 實體轉換成 UserDTO，用於 API 或前端傳輸。
    public static UserDTO fromUser(User user) {

        UserDTO userDTO = new UserDTO(); // 建立一個新的 UserDTO。

        BeanUtils.copyProperties(user, userDTO);
        //使用 BeanUtils.copyProperties(user, userDTO) 把 User 的屬性複製到 UserDTO

        return userDTO; //回傳轉換後的 UserDTO。
    } // 例如：User(name="Katie", email="katie@example.com")
      //      轉換成 UserDTO(name="Katie", email="katie@example.com")。


/*
Right now your fromUser(...) 方法只是建立並回傳一個 UserDTO 物件。
如果你在 System.out.println(userDTO) 時只看到像 UserDTO@52f8bc4 這樣的輸出，那是因為 UserDTO 沒有覆寫 toString()，Java 只會用預設的 Object.toString() 格式。
 */
    public static UserDTO fromUser(User user, Role role) { //role name, role permission
    /*
       📖 為什麼需要在 UserDTO 裡加上 roleName 和 permissions :
        1. DTO 要承載前端需要的資料
            UserDTO 是專門用來傳輸資料給前端或其他系統的物件。
            當你在 UserDTOMapper.fromUser(User user, Role role) 方法裡傳入了 Role，就表示你希望把使用者的角色資訊也一併帶到 DTO。
            如果 UserDTO 沒有 roleName 和 permissions 欄位，那麼即使你傳入了 Role，也沒有地方存放這些資料。

       2. BeanUtils.copyProperties 只能複製相同欄位
            BeanUtils.copyProperties(user, userDTO) 只會把 User 裡面和 UserDTO 欄位名稱相同的屬性複製過去。
            但 User 實體通常不會直接有 roleName 或 permissions 欄位，它可能只有一個 Role 物件。
            所以你需要在 UserDTO 裡額外定義 roleName 和 permissions，才能在 Mapper 裡手動設定：
            userDTO.setRoleName(role.getName());
            userDTO.setPermissions(role.getPermission());

       3. 分離資料結構，避免前端直接接觸 Role 實體
            如果你不在 DTO 裡加上 roleName 和 permissions，前端就必須直接處理 Role 物件。
            這樣會破壞 DTO 的設計初衷：只傳輸必要且安全的資料。
            把角色資訊展開成字串 (roleName) 和權限 (permissions)，前端就能直接使用，而不用理解後端的 Role 結構。

     */
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        userDTO.setRoleName(role.getName());//pass the roleName to the userDTO
        userDTO.setPermissions(role.getPermission());//pass the permission to the role,
        return userDTO;
        /*
        宣告一個靜態方法，名稱是 fromUser。
        參數：
        User user → 使用者的完整實體物件 (通常包含資料庫欄位，例如 id、email、password、createdDate)。
        Role role → 使用者的角色物件 (例如 ADMIN、USER)，雖然在這段程式碼裡沒有被使用。
        回傳型別是 UserDTO，代表轉換後的資料傳輸物件。

        📖 為什麼需要這樣做？
            分離資料層與展示層
            User 實體通常直接對應資料庫，可能包含敏感資訊 (例如密碼)。
            UserDTO 是專門用來傳輸或回應前端的物件，只保留必要資訊。
            避免洩漏敏感資料
            使用 DTO 可以避免直接把 User 實體傳給前端，確保安全性。
            提高程式維護性
            如果 User 實體欄位改變，只要調整 UserDTO 與 Mapper，不需要改動所有使用者 API。

       📖 總結 :
            這個 fromUser 方法的作用是：
            建立一個新的 UserDTO。
            把 User 實體的屬性複製到 UserDTO。
            回傳轉換後的 UserDTO，供前端或其他層使用。
         */
    }

    // toUser：把 UserDTO 轉換回 User 實體，用於資料庫操作。
    public static User toUser(UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        return user;
    }
}
