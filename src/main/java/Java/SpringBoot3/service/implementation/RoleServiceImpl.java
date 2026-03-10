package Java.SpringBoot3.service.implementation;

import Java.SpringBoot3.domain.Role;
import Java.SpringBoot3.repository.RoleRepository;
import Java.SpringBoot3.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;


@Service
@RequiredArgsConstructor

public class RoleServiceImpl implements RoleService {

    /*
    宣告 private final RoleRepository<Role> roleRoleRepository;，
    並搭配 @RequiredArgsConstructor，Spring 會自動把 RoleRepository 的實例注入進來。

    RoleRepository 是資料存取層 (DAO/Repository)，負責查詢、儲存、更新、刪除 Role 資料。
    在這裡，roleRoleRepository.getRoleByUserId(id) 就是呼叫 Repository 的方法，透過使用者 ID 查詢該使用者的角色。
    Service 層只需要呼叫 Repository，不必自己寫 SQL 或資料庫邏輯。
     */
    private final RoleRepository<Role> roleRoleRepository;
    /*
    ✅ 一句話總結：
    RoleRepository<Role> 的 Role 是泛型型別，指定這個 Repository 專門處理角色物件；
    而 roleRoleRepository 的「role」只是變數名稱，讓人知道它是角色的 Repository。

    第一個 Role → 泛型型別，告訴 Repository 要操作 Role 物件。
    第二個 roleRoleRepository → 變數名稱，代表這個 Repository 的實例，命名只是為了清楚表達用途。
     */

    @Override
    public Role getRoleByUserId(Long id) {
        return roleRoleRepository.getRoleByUserId(id);
    }

    @Override
    public Collection<Role> getRoles() {
        return roleRoleRepository.List();
        //return List.of(); 这里虽然能编译，但你只是返回了一个空集合，并没有真正调用数据库。正确做法应该是调用 RoleRepository.List() 方法：
    }
}
