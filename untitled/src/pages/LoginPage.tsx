import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginUser } from "../api/authApi";

export default function LoginPage() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();

    const handleLogin = async () => {
        const response = await loginUser(email, password);
        if (response) {
            navigate("/dashboard");
        } else {
            alert("Неверные данные");
        }
    };

    const handleRedirectToRegister = () => {
        navigate("/register");
    };

    return (
        <div style={{ marginTop: "100px", textAlign: "center" }}>
            <h2>Вход</h2>
            <input
                type="text"
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                style={{ display: "block", margin: "10px auto", padding: "8px" }}
            />
            <input
                type="password"
                placeholder="Пароль"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{ display: "block", margin: "10px auto", padding: "8px" }}
            />
            <button onClick={handleLogin} style={{ padding: "10px 20px", margin: "10px" }}>
                Войти
            </button>
            <br />
            <button onClick={handleRedirectToRegister} style={{ padding: "8px 16px", marginTop: "10px" }}>
                Зарегистрироваться
            </button>
        </div>
    );
}
