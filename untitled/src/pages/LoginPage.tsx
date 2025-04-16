import React, { useState } from "react";
import {
    Box,
    Button,
    Card,
    CardContent,
    TextField,
    Typography,
} from "@mui/material";
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
        <Box
            display="flex"
            justifyContent="center"
            alignItems="center"
            minHeight="100vh"
            bgcolor="#f5f5f5"
        >
            <Card sx={{ width: 400, padding: 3, boxShadow: 3 }}>
                <CardContent>
                    <Typography variant="h5" textAlign="center" gutterBottom>
                        Вход
                    </Typography>
                    <TextField
                        label="Email"
                        variant="outlined"
                        fullWidth
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        sx={{ mb: 2 }}
                    />
                    <TextField
                        label="Пароль"
                        type="password"
                        variant="outlined"
                        fullWidth
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        sx={{ mb: 3 }}
                    />
                    <Button
                        variant="contained"
                        onClick={handleLogin}
                        fullWidth
                        sx={{ mb: 1 }}
                    >
                        Войти
                    </Button>
                    <Button onClick={handleRedirectToRegister} fullWidth>
                        Зарегистрироваться
                    </Button>
                </CardContent>
            </Card>
        </Box>
    );
}
