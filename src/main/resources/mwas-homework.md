# MWAS Project

In this project you have a hotel reservation application. Your task is to implement authentication and authorization for the application. The application should be able to authenticate users using multiple methods and should have a role hierarchy. The application should also have a REST API that should be secured using JWT tokens.

Here are the security requirements for the application: 
1. Tha application should enable multiple methods for authentication. There are 2 types of users: `ADMIN` and `USER`.    
   - The users should be able to authenticate using the following ways: 
     1. **(10 pt)** Using the Employees from saved in the application. If the employee is manager, the user should have the role `ADMIN`, otherwise the role `USER`. There are two users in the database: `manager` and `user`, both with the password `pwd`.
     2. **(10 pt)** Using the Keycloak server configured for the DTI course via OAuth2 protocol. The role should be extracted from the `scope` claim. 
     3. **(10 pt)** Using the LDAP server configured for the DTI course (directly, not through the Keycloak server). The role should be extracted from the `manager` and `employee` group membership.
     4. **(10 pt)** Using the IDP provider of your choice (ex.: Google, Facebook, GitHub, etc.). Everyone authenticated with the IDP should be in role `USER`.
   - **(5 pt)** Role hierarchy should be implemented, unifying the roles and scopes from the database, keycloak scopes and LDAP mapped roles. The `ADMIN` role should have all the permissions of the `USER` role.
   - **(5 pt)** The remember me functionality should be implemented.
2. Configure the authorization in the following way:
    - **(5 pt)** The home page ("/") and the reservations page ("/reservations") should be accessible to everyone.
    - **(5 pt)** The extend reservation pages (everything starting with "/reservations/extend/...") should be accessible only to users with the role `USER`.
    - **(5 pt)** All other pages should be accessible only to users with the role `ADMIN`.
    - **(10 pt)** The `ReservationServiceImpl.listAll` method should return only the reservations of the hotel of the logged in Employee. If the user is not linked to the employee, the method should return an empty list.
3. **(25 pt)** Configure JWT Authentication for the API and generate a JWT Token on Login
   - When someone access the `/api/**` endpoint, the JWT token should be validated and the user should be authenticated. All api points require only authentication. 
   - If the token is not valid or not present, `401 Unauthorized` should be returned.
   - Configure **username** and **password** authentication for the API that need to be processed at the `/api/login` endpoint. After the login, the user should receive a JWT token.
     - The token should be valid for 1 hour.
   - The token should be sent by the clients in the `Authorization` header as a `Bearer` token.



