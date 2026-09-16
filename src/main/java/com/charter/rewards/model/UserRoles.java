package com.charter.rewards.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_roles")
public class UserRoles {

  @Id
  @Column(name = "user_id")
  private Long userId;
  @Id
  @Column(name = "role_id")
  private Long roleId;

  public UserRoles() {
  }

  public UserRoles(Long userId, Long roleId) {
    this.userId = userId;
    this.roleId = roleId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getRoleId() {
    return roleId;
  }

  public void setRoleId(Long roleId) {
    this.roleId = roleId;
  }

}
