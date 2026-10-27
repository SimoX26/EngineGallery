package it.SimoSW.util.security;

import it.SimoSW.model.User;
import it.SimoSW.model.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserAccessPolicyTest {
    @Test
    void everyOperatorCannotAccessWarehouseOrStatistics() {
        for (String username : new String[] {"maurizio", "Manuel", "mario"}) {
            User user = new User(username, null, UserRole.OPERATOR);
            assertFalse(UserAccessPolicy.canViewWarehouse(user));
            assertFalse(UserAccessPolicy.canViewStatistics(user));
            assertTrue(UserAccessPolicy.isDeniedPath(user, "/warehouse/list"));
            assertTrue(UserAccessPolicy.isDeniedPath(user, "/warehouse/edit"));
            assertTrue(UserAccessPolicy.isDeniedPath(user, "/uploads/warehouse/1/photo.jpg"));
            assertTrue(UserAccessPolicy.isDeniedPath(user, "/statistics"));
            assertFalse(UserAccessPolicy.isDeniedPath(user, "/workshop-maintenance"));
        }
    }

    @Test
    void accessDependsOnRoleRatherThanUsername() {
        User admin = new User("maurizio", null, UserRole.ADMIN);
        assertTrue(UserAccessPolicy.canViewWarehouse(admin));
        assertTrue(UserAccessPolicy.canViewStatistics(admin));
        assertFalse(UserAccessPolicy.isDeniedPath(admin, "/warehouse/list"));
        assertFalse(UserAccessPolicy.isDeniedPath(admin, "/statistics"));
        assertFalse(UserAccessPolicy.isDeniedPath(new User("maurizio", null, UserRole.OPERATOR), "/warehouse-tools"));
        User inspector = new User("ispettore", null, UserRole.INSPECTOR);
        assertTrue(UserAccessPolicy.canViewWarehouse(inspector));
        assertFalse(UserAccessPolicy.canViewStatistics(inspector));
    }

    @Test
    void onlyAdministratorsCanAccessVehicles() {
        assertFalse(UserAccessPolicy.isDeniedPath(new User("admin", null, UserRole.ADMIN), "/vehicles"));
        for (UserRole role : new UserRole[] {UserRole.OPERATOR, UserRole.INSPECTOR}) {
            User user = new User("user", null, role);
            assertTrue(UserAccessPolicy.isDeniedPath(user, "/vehicles"));
            assertTrue(UserAccessPolicy.isDeniedPath(user, "/vehicles/1"));
        }
        assertFalse(UserAccessPolicy.isDeniedPath(new User("user", null, UserRole.OPERATOR), "/vehicles-old"));
    }
}
