import React, { useState, FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { registerUser, loginUser } from "../api/authApi";
import {
    Box,
    Button,
    Card,
    CardContent,
    TextField,
    Typography,
} from "@mui/material";

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
        <Box
            display="flex"
            justifyContent="center"
            alignItems="center"
            minHeight="100vh"
            bgcolor="#f5f5f5"
        >
            <Card sx={{ width: 400, padding: 3, boxShadow: 3 }}>
                <CardContent>
                    <Typography variant="h5" component="h2" textAlign="center" gutterBottom>
                        Регистрация
                    </Typography>
                    <Box component="form" onSubmit={handleSubmit}>
                        <TextField
                            label="Имя пользователя"
                            value={userName}
                            onChange={(e) => setUserName(e.target.value)}
                            fullWidth
                            margin="normal"
                            required
                        />
                        <TextField
                            label="Почта"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            fullWidth
                            margin="normal"
                            required
                        />
                        <TextField
                            label="Пароль"
                            type="password"
                            value={password1}
                            onChange={(e) => setPassword1(e.target.value)}
                            fullWidth
                            margin="normal"
                            required
                        />
                        <TextField
                            label="Повторите пароль"
                            type="password"
                            value={password2}
                            onChange={(e) => setPassword2(e.target.value)}
                            fullWidth
                            margin="normal"
                            required
                        />
                        <Button
                            type="submit"
                            variant="contained"
                            color="primary"
                            fullWidth
                            sx={{ mt: 2 }}
                        >
                            Зарегистрироваться
                        </Button>
                    </Box>
                    {message && (
                        <Typography color="error" textAlign="center" mt={2}>
                            {message}
                        </Typography>
                    )}
                </CardContent>
            </Card>
        </Box>
    );
};

export default Register;
