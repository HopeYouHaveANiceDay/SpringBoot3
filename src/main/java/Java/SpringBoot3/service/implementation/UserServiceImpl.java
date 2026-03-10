package Java.SpringBoot3.service.implementation;

import Java.SpringBoot3.domain.Role;
import Java.SpringBoot3.domain.User;
import Java.SpringBoot3.dto.UserDTO;

import Java.SpringBoot3.form.UpdateForm;
import Java.SpringBoot3.repository.RoleRepository;
import Java.SpringBoot3.repository.UserRepository;
import Java.SpringBoot3.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static Java.SpringBoot3.dtomapper.UserDTOMapper.fromUser;

@Service//標註這是一個 Spring 的 Service 類別，會被 Spring 容器管理。
@RequiredArgsConstructor//並注入 final 欄位。這裡的 userRepository 和 roleRoleRepository 會自動透過建構子注入。

// implements UserService => 表示這個類別是 UserService 介面的具體實作。
public class UserServiceImpl implements UserService {

    // userRepository → 與資料庫互動，負責使用者相關的 CRUD 操作。
    private final UserRepository<User> userRepository;

    // roleRoleRepository → 與角色資料庫互動，用來查詢使用者的角色。
    private final RoleRepository<Role> roleRoleRepository; // have RoleRepository -> means that we get user role by calling RoleRepository

/*
這樣的設計方式：
    介面 (UserService) → 定義規範。
    實作類別 (UserServiceImpl) → 實際執行邏輯。
    Repository (UserRepository) → 與資料庫互動。
    DTO (UserDTO) → 對外傳輸資料，避免暴露敏感資訊。

 ** UserServiceImpl.java，屬於 Spring Boot 專案中的 Service 層實作類別。
    它負責處理使用者相關的業務邏輯，
    並透過 Repository 與資料庫互動，再將結果轉換成 UserDTO 回傳。
 */


/*
✅ 一句話總結：
fromUser 是純粹的轉換器，適合在你已經有 User 和 Role 的情況下使用；
mapToUserDTO 是封裝方法，適合在你只有 User，需要額外查角色並轉換成完整 UserDTO 的情況下使用。
*/
    @Override
    public UserDTO createUser(User user) {
        return mapToUserDTO(userRepository.create(user));
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        return mapToUserDTO(userRepository.getUserByEmail(email));
    }

    @Override
    public void sendVerificationCode(UserDTO user) {
        userRepository.sendVerificationCode(user);
    }

    @Override
    public UserDTO verifyCode(String email, String code) {
        return mapToUserDTO(userRepository.verifyCode(email, code));
    }

    @Override
    public void resetPassword(String email) {
         userRepository.resetPassword(email);
    }

    @Override
    public UserDTO verifyPasswordKey(String key) {
        return mapToUserDTO(userRepository.verifyPasswordKey(key));
    }

    @Override
    public void updatePassword(Long userId, String password, String confirmPassword) {
        userRepository.renewPassword(userId, password, confirmPassword);
    }

    @Override
    public UserDTO verifyAccountKey(String key) {
        return mapToUserDTO(userRepository.verifyAccountKey(key));
    }

    @Override
    public UserDTO updateUserDetails(UpdateForm user) {
        return mapToUserDTO(userRepository.updateUserDetails(user));
    }

    @Override
    public UserDTO getUserById(Long userId) {
        return mapToUserDTO(userRepository.get(userId));
    }

    @Override
    public void updatePassword(Long id, String currentPassword, String newPassword, String confirmNewPassword) {
        userRepository.updatePassword(id, currentPassword, newPassword, confirmNewPassword);
    }

    @Override
    public void updatedUserRole(Long userId, String roleName) {
        roleRoleRepository.updateUserRole(userId, roleName);
    }

    @Override
    public void updateAccountSettings(Long userId, Boolean enabled, Boolean notLocked) {
        userRepository.updateAccountSettings(userId, enabled, notLocked);
    }

    @Override
    public UserDTO toggleMfa(String email) {
        return mapToUserDTO(userRepository.toggleMfa(email));
    }


    //這個方法的設計非常簡單，主要是把「更新使用者圖片」的責任交給 userRepository。
    @Override
    // 接收兩個參數：
    //  => UserDTO user：使用者資料傳輸物件，通常包含使用者的基本資訊（例如 ID、email）。
    //  =>  MultipartFile image：Spring 提供的檔案上傳物件，代表前端上傳的圖片。
    public void updateImage(UserDTO user, MultipartFile image) {
        userRepository.updateImage(user, image);
        /*
        方法內容 :
            userRepository.updateImage(user, image);
            呼叫 Repository 層的方法，把圖片更新到資料庫（或檔案系統）。
            Repository 層通常會負責：
            把圖片存到伺服器檔案系統或雲端（例如 AWS S3）。
            更新資料庫裡的使用者紀錄，存放圖片路徑或 URL。
         */
    }

    private UserDTO mapToUserDTO(User user) {
        return fromUser(user, roleRoleRepository.getRoleByUserId(user.getId()));
    /*
    private
        方法是私有的，只能在這個類別內部使用。
    UserDTO mapToUserDTO(User user)
        方法名稱：mapToUserDTO，意思是「把 User 映射成 UserDTO」。
        參數：User user → 從資料庫查詢到的使用者物件。
        回傳型別：UserDTO → 專門用來傳輸到前端或其他系統的資料物件。
    roleRoleRepository.getRoleByUserId(user.getId())
        根據使用者的 ID，從 roleRoleRepository 查詢該使用者的角色 (Role)。
        角色通常包含角色名稱 (roleName) 和權限 (permissions)。
    fromUser(user, role)
        呼叫 fromUser 方法，把 User 和 Role 一起轉換成 UserDTO。
        fromUser 通常會使用 BeanUtils.copyProperties 複製使用者基本欄位，並額外設定角色名稱與權限。
    */
    }
}



    /*
    Don't use the "User" here!!!
    //@Override
    //public User getUser(String email) {
        //return userRepository.getUserByEmail(email);
    4. 透過 Email 取得完整使用者 :
        getUser(String email) → 透過 Email 查詢完整的 User 實體。
        與 getUserByEmail 不同的是，這裡回傳的是完整的 User，而不是 UserDTO。
        通常用於內部邏輯，不直接暴露給前端。
    }  */


/*
public class Animal {
    public void makeSound() {
        System.out.println("Some sound");
    }
}

public class Dog extends Animal {
    @Override
    public void makeSound() {
        System.out.println("Woof!");
    }
}

Example Usage :
    Animal a = new Animal();
    a.makeSound();   // Output: Some sound

    Dog d = new Dog();
    d.makeSound();   // Output: Woof!

    Animal ad = new Dog();
    ad.makeSound();  // Output: Woof! (because of polymorphism)

**************************
1. Why does Dog need to extend Animal?
        Inheritance: By writing class Dog extends Animal, you’re saying Dog is a type of Animal.
        This means Dog automatically gets all the methods and properties of Animal (like makeSound()), but it can override them with its own behavior.
        Without extends Animal, Dog would just be a standalone class, and you couldn’t treat it as an Animal.

2. What is Animal ad = new Dog();?
        This is polymorphism in action.
        You’re declaring a variable of type Animal (ad), but assigning it an object of type Dog.
        Even though the reference type is Animal, the actual object is a Dog.
        When you call ad.makeSound(), Java looks at the actual object type (Dog) and runs Dog’s version of makeSound().
        → That’s why it prints "Woof!" instead of "Some sound".

3. Why not just Dog ad = new Dog();?
        You can do that, and it works fine.
        But using Animal ad = new Dog(); is more powerful because:
            You can store different subclasses (Dog, Cat, Bird) in the same Animal variable or collection.
            Your code becomes more flexible and reusable. For example:

Animal a1 = new Dog();
Animal a2 = new Cat();
Animal a3 = new Bird();

List<Animal> animals = Arrays.asList(a1, a2, a3);
for (Animal a : animals) {
    a.makeSound();  // Each prints its own sound!
}
This way, you don’t need separate lists or methods for each animal type.


4. What is polymorphism?
        Polymorphism means “many forms”.
        In Java, it allows one interface or superclass (Animal) to be used with different underlying forms (Dog, Cat, etc.).
        The method that gets executed depends on the actual object type at runtime, not the reference type.

5. Why use polymorphism?
        Flexibility: You can write code that works on the general type (Animal) but behaves correctly for each specific subtype (Dog, Cat, etc.).
        Maintainability: You don’t need giant if/else blocks checking the type of animal — each subclass knows how to behave.
        Extensibility: Adding a new animal (say, Horse) doesn’t require changing existing code. Just override makeSound() in Horse.

✅ In short:
        Dog extends Animal → Dog inherits Animal’s behavior but can override it.
        Animal ad = new Dog(); → Reference is general (Animal), object is specific (Dog).
        Polymorphism → Lets you write flexible code that adapts to the actual object type at runtime.

**** If you don’t override it,
        calling makeSound() on a Dog object would just run the Animal version and print:
        Some sound
        So inheritance means: Dog gets Animal’s methods and properties for free.


public class Animal {
    public void makeSound() {
        System.out.println("Some sound");
    }
}

public class Dog extends Animal {
    @Override
    public void makeSound() {
        System.out.println("Woof!");
    }
}
------->
@Override
public void makeSound() {
    System.out.println("Woof!");
}
Now, when you call makeSound() on a Dog, Java uses the overridden version and prints:
Woof!

=============

In Java, each public class must be defined in its own file, and the file name must match the class name. That’s why:
    Animal goes in a file called Animal.java
    Dog goes in a file called Dog.java
Both files should be in the same package (or at least accessible to each other).



How @Override works?
    You use @Override when a subclass (like Dog) provides its own version of a method that already exists in the superclass (Animal).
    In your case, Dog overrides makeSound() from Animal.
    You don’t need to (and cannot) put @Override in the Animal class itself — only in the subclass that is overriding.

(1) Example Project Structure
    src/
     └── com/example/
          ├── Animal.java
          └── Dog.java

(2) Animal.java
    package com.example;

    public class Animal {
        public void makeSound() {
            System.out.println("Some sound");
        }
    }

(3) Dog.java
    package com.example;

    public class Dog extends Animal {
        @Override
        public void makeSound() {
            System.out.println("Woof!");
        }
    }

(4) Using Them Together
    You can then create a test file (say Main.java) to run them:
    package com.example;
    public class Main {
        public static void main(String[] args) {
            Animal a = new Animal();
            a.makeSound();   // Output: Some sound

            Dog d = new Dog();
            d.makeSound();   // Output: Woof!

            Animal ad = new Dog();
            ad.makeSound();  // Output: Woof! (polymorphism)
        }
    }

✅ Key takeaway:
Each public class goes in its own .java file.
@Override is used only in the subclass (Dog) to replace the inherited method.
You then use these classes together in another file (like Main.java) to run the program.
 */

