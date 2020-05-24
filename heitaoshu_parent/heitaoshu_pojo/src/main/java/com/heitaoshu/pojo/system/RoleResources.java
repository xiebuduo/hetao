package com.heitaoshu.pojo.system;

import java.io.Serializable;
import java.util.List;

public class RoleResources implements Serializable {
    private Role role;
    private List<RoleResource> roleResources;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<RoleResource> getRoleResources() {
        return roleResources;
    }

    public void setRoleResources(List<RoleResource> roleResources) {
        this.roleResources = roleResources;
    }
}
