package com.mekcone.studio.service.web;

import com.mekcone.studio.entity.DTO.User;

/*
 * Author: Tenton Lien
 * Date: 10/1/2020
 */

public interface AuthenticationService {

    /**
     * authUser
     *
     * @param username username
     * @param password password
     * @return boolean
     */
    boolean authUser(String username, String password);

    /**
     * authUser
     *
     * @param user     user
     * @param username username
     * @param password password
     * @return boolean
     */
    boolean authUser(User user, String username, String password);
}
