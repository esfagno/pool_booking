package com.poolapp.pool.service;

public interface AdminService {
    void promoteToAdmin(String email);

    void demoteToUser(String email);
}

