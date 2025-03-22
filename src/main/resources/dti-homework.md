
# Digital Trust and Identity Project

The goal of the Digital Trust and Identity project is to configure LDAP and Keycloak servers and implement authentication and authorization for a hotel chain application. The application should be able to authenticate users using multiple methods and should have a role hierarchy.

It is recommended to install the LDAP and Keycloak servers on the virtual machines hosted at 185.153.49.(180-190) for which you were sent instructions by email. However, you can install the servers on your local machine or on other virtual machines.  

## Ldap requirements

Configure LDAP server containing user information for a hotel chain corporate network. The network should have 2 hotels, and in each hotel there are groups for managers and employees. The LDAP server should contain the following information:

- 2 hotels
- 2 managers for each hotel
- 5 employees for each hotel
- 1 group for managers in each hotel
- 1 group for employees in each hotel
- 1 group for all employees in each hotel
- 1 group for all users in each hotel
- 1 group for all users in the corporate network
- 1 group for all managers in the corporate network
- 1 group for all employees in the corporate network

## Keycloak requirements
1. Install keycloak on the virtual machine. 
2. Create one realm for the hotel chain. 
3. In the realm, create the following roles:
- manager
- employee
- guest
4. Create 2 users, one manager and one employee, and assign the corresponding roles to the users.
5. Create scopes for each role and assign the scopes to the roles.
6. Create a OAuth2 client for the hotel chain application and configure the created scopes as optional scopes for the client.
7. Configure and test the login and logout functionality using the mwas application (or other client of your choice).
8. Configure user federation to the created LDAP server and test the login and logout functionality using the mwas application (or other client of your choice).
9. Configure IDP provider of your choice and test the login and logout functionality using the mwas application (or other client of your choice).