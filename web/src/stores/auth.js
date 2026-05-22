import { defineStore } from "pinia";
import { loginApi, meApi } from "../api/modules/authApi";

const TOKEN_KEY = "ise_token";
const PROFILE_KEY = "ise_profile";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: "",
    user: null,
    roles: [],
    permissions: [],
    dataScopes: [],
    initialized: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    roleCode: (state) => state.roles[0] ?? "",
  },
  actions: {
    async restoreSession() {
      const token = localStorage.getItem(TOKEN_KEY);
      const profileRaw = localStorage.getItem(PROFILE_KEY);

      if (!token || !profileRaw) {
        this.initialized = true;
        return;
      }

      try {
        this.token = token;
        const parsedProfile = JSON.parse(profileRaw);
        this.user = parsedProfile.user;
        this.roles = parsedProfile.roles;
        this.permissions = parsedProfile.permissions;
        this.dataScopes = parsedProfile.dataScopes;

        const profile = await meApi();
        this.applyProfile(profile);
      } catch {
        this.clearSession();
      } finally {
        this.initialized = true;
      }
    },
    async login(payload) {
      const result = await loginApi(payload);
      this.token = result.token;
      this.applyProfile(result);
      localStorage.setItem(TOKEN_KEY, result.token);
      localStorage.setItem(
        PROFILE_KEY,
        JSON.stringify({
          user: this.user,
          roles: this.roles,
          permissions: this.permissions,
          dataScopes: this.dataScopes,
        }),
      );
      this.initialized = true;
      return result;
    },
    applyProfile(profile) {
      this.user = profile.user;
      this.roles = profile.user?.roles ?? profile.roles ?? [];
      this.permissions = profile.user?.permissions ?? profile.permissions ?? [];
      this.dataScopes = profile.user?.dataScopes ?? profile.dataScopes ?? [];
    },
    clearSession() {
      this.token = "";
      this.user = null;
      this.roles = [];
      this.permissions = [];
      this.dataScopes = [];
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(PROFILE_KEY);
    },
    logout() {
      this.clearSession();
    },
    hasPermission(code) {
      return this.permissions.includes(code);
    },
  },
});
