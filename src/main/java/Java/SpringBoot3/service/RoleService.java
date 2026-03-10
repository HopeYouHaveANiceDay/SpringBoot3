package Java.SpringBoot3.service;

import Java.SpringBoot3.domain.Role; //引入了 Role 类，这个类应该定义在你的 domain 包里，用来表示用户的角色（比如 ADMIN、USER 等）。

import java.util.Collection;

//RoleService 是一个接口，定义了角色相关的业务方法，Service 层负责业务逻辑。
public interface RoleService {

    /*
    方法 1：Role getRoleByUserId(Long id);
            根据用户的 ID 获取该用户的角色。
            参数：用户 ID (Long id)
            返回值：一个 Role 对象    */
    Role getRoleByUserId(Long id);

    /*
    方法 2：Collection<Role> getRoles();
        获取所有角色的集合。
        返回值：一个 Collection<Role>，里面包含多个角色对象。     */
    Collection<Role> getRoles();
}
