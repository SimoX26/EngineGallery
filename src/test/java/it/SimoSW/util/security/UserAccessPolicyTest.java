package it.SimoSW.util.security;

import it.SimoSW.model.User;
import it.SimoSW.model.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserAccessPolicyTest {
    @Test
    void maurizioAndManuelCannotAccessWarehouseOrStatisticsEvenWithAdminRole() {
        for (String username : new String[] {"maurizio", "Manuel"}) {
            User user = new User(username, null, UserRole.ADMIN);
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
    void otherUsersKeepExistingAccess() {
        User operator = new User("mario", null, UserRole.OPERATOR);
        User admin = new User("rml", null, UserRole.ADMIN);
        assertTrue(UserAccessPolicy.canViewWarehouse(operator));
        assertFalse(UserAccessPolicy.canViewStatistics(operator));
        assertTrue(UserAccessPolicy.canViewWarehouse(admin));
        assertTrue(UserAccessPolicy.canViewStatistics(admin));
        assertFalse(UserAccessPolicy.isDeniedPath(operator, "/warehouse/list"));
        assertFalse(UserAccessPolicy.isDeniedPath(admin, "/statistics"));
        assertFalse(UserAccessPolicy.isDeniedPath(new User("maurizio", null, UserRole.OPERATOR), "/warehouse-tools"));
    }
}
