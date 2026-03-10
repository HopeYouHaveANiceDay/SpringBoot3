package Java.SpringBoot3.dto;


import lombok.*;

import java.time.LocalDateTime;
/*
分離資料層與展示層 :
User 實體通常直接對應資料庫，可能包含敏感資訊 (例如密碼)。
UserDTO 是專門用來傳輸或回應前端的物件，只保留必要資訊。
 */

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
// without @ToString + UserResource.java add "System.out.println(((UserPrincipal) authentication.getPrincipal()).getUser());"
//      => the terminal show => Java.SpringBoot3.dto.UserDTO@52f8bc3
// @ToString + UserResource.java add "System.out.println(((UserPrincipal) authentication.getPrincipal()).getUser());"
//      => access entire user information from authentication
//      => the terminal show => the terminal output: UserDTO(id=4, firstName=Katie, lastName=Fan, email=KatieFan@gmail.com, address=null, phone=null, title=null, bio=null, imageUrl=https://cdn-icons-png.flaticon.com/512/149/149071.png, enabled=true, isNotLocked=true, isUsingMfa=false, createdAt=2026-01-13T14:48:43, roleName=ROLE_USER, permissions=READ:USER,READ:CUSTOMER)
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    //get rid of the password because we don't want to ever send that to the frontend or to caller
    private String address;
    private String phone;//電話號碼雖然由數字組成，但它不是拿來做數學計算的。電話號碼常常包含 +、-、空格、括號 等符號。例如：+852-1234-1234。如果用 int 或 long，這些符號就無法保存。
    private String title;
    private String bio;
    private String imageUrl;
    private boolean enabled;
    private boolean isNotLocked;
    private boolean isUsingMfa;
    private LocalDateTime createdAt;
    private String roleName;
    private String permissions;
}
