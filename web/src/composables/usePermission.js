import { computed } from "vue";
import { useAuthStore } from "../stores/auth";

export function usePermission() {
  const authStore = useAuthStore();

  const roleCode = computed(() => authStore.roleCode);
  const permissions = computed(() => authStore.permissions);
  const dataScopes = computed(() => authStore.dataScopes);

  function hasPermission(code) {
    return authStore.hasPermission(code);
  }

  function hasAnyPermission(codes) {
    return codes.some((code) => authStore.hasPermission(code));
  }

  function hasRole(role) {
    return authStore.roles.includes(role);
  }

  return {
    roleCode,
    permissions,
    dataScopes,
    hasPermission,
    hasAnyPermission,
    hasRole,
  };
}
