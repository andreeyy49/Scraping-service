import React, { useState, FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { registerUser, loginUser } from "../api/authApi";

const Register: React.FC = () => {
    const [email, setEmail] = useState("");
    const [userName, setUserName] = useState("");
    const [password1, setPassword1] = useState("");
    const [password2, setPassword2] = useState("");
    const [message, setMessage] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setMessage("");

        if (password1 !== password2) {
            setMessage("Пароли не совпадают");
            return;
        }

        const registrationResult = await registerUser(email, password1, userName);
        if (!registrationResult) {
            setMessage("Регистрация не удалась");
            return;
        }

        const loginResult = await loginUser(email, password1);
        if (!loginResult || !loginResult.refreshToken) {
            localStorage.removeItem("token");
            localStorage.removeItem("refreshToken");
            setMessage("Логин после регистрации не удался");
            return;
        }

        navigate("/dashboard");
    };


    return (
        <div style={{ marginTop: "100px", textAlign: "center" }}>
            <h2>Регистрация</h2>
            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    placeholder="Имя пользователя"
                    value={userName}
                    onChange={(e) => setUserName(e.target.value)}
                    required
                    style={{ display: "block", margin: "10px auto", padding: "8px" }}
                />
                <input
                    type="email"
                    placeholder="Почта"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    style={{ display: "block", margin: "10px auto", padding: "8px" }}
                />
                <input
                    type="password"
                    placeholder="Пароль"
                    value={password1}
                    onChange={(e) => setPassword1(e.target.value)}
                    required
                    style={{ display: "block", margin: "10px auto", padding: "8px" }}
                />
                <input
                    type="password"
                    placeholder="Повторите пароль"
                    value={password2}
                    onChange={(e) => setPassword2(e.target.value)}
                    required
                    style={{ display: "block", margin: "10px auto", padding: "8px" }}
                />
                <button type="submit" style={{ padding: "10px 20px" }}>
                    Зарегистрироваться
                </button>
            </form>
            {message && <p>{message}</p>}
        </div>
    );
};

export default Register;
