// src/api/authApi.ts
import axios from "axios";

const API_URL = "http://localhost:8080/api/v1/auth"; // URL твоего аутентификационного сервиса

export const loginUser = async (email: string, password: string) => {
    try {
        const response = await axios.post(`${API_URL}/login`, { email, password });
        const { token, refreshToken } = response.data;

        // Сохраняем оба токена
        localStorage.setItem("token", token);
        localStorage.setItem("refreshToken", refreshToken);

        return { token, refreshToken };
    } catch (error) {
        console.error("Ошибка при авторизации", error);
        return null;
    }
};


// Функция для регистрации
export const registerUser = async (email: string, password: string, userName: string) => {
    try {
        const response = await axios.post(`${API_URL}/register`, { email, password1: password, password2: password, userName });
        return response.data;
    } catch (error) {
        console.error("Ошибка при регистрации", error);
        return null;
    }
};

export const refreshAccessToken = async () => {
    const refreshToken = localStorage.getItem("refreshToken");

    if (!refreshToken) {
        console.error("Refresh token не найден");
        return null;
    }

    try {
        const response = await axios.post(`${API_URL}/refresh`, { refreshToken });
        const { token: newAccessToken } = response.data;

        localStorage.setItem("token", newAccessToken);
        return newAccessToken;
    } catch (error) {
        console.error("Ошибка при обновлении токена", error);
        return null;
    }
};

axios.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

axios.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // Проверяем, не обновляем ли мы токен уже и не повторяем ли запрос
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            const newToken = await refreshAccessToken();
            if (newToken) {
                axios.defaults.headers.common["Authorization"] = `Bearer ${newToken}`;
                originalRequest.headers["Authorization"] = `Bearer ${newToken}`;
                return axios(originalRequest); // Повторяем оригинальный запрос
            }
        }

        return Promise.reject(error);
    }
);