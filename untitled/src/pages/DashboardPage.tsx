import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import axios from "axios";
import {
    Box,
    Card,
    CardContent,
    CircularProgress,
    Typography,
    Container,
} from "@mui/material";

export default function DashboardPage() {
    const [data, setData] = useState<any>(null);
    const [isCheckingAuth, setIsCheckingAuth] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem("token");

        if (!token || token === "undefined" || token === "null") {
            navigate("/login");
        } else {
            const fetchData = async () => {
                try {
                    const response = await axios.get("http://localhost:8080/api/v1/protected-resource", {
                        headers: {Authorization: `Bearer ${token}`},
                    });
                    setData(response.data);
                } catch (error) {
                    console.error("Ошибка при получении данных", error);
                } finally {
                    setIsCheckingAuth(false);
                }
            };
            fetchData();
        }
    }, [navigate]);

    if (isCheckingAuth) {
        return (
            <Box
                display="flex"
                justifyContent="center"
                alignItems="center"
                minHeight="100vh"
                bgcolor="#f5f7fa"
            >
                <CircularProgress/>
            </Box>
        );
    }

    return (
        <Container maxWidth="md" sx={{mt: 8}}>
            <Typography variant="h4" align="center" gutterBottom>
                👋 Добро пожаловать!
            </Typography>

            <Card sx={{mt: 4, boxShadow: 3}}>
                <CardContent>
                    <Typography variant="h6" gutterBottom>
                        📊 Данные пользователя
                    </Typography>

                    {data ? (
                        <Box
                            component="pre"
                            sx={{
                                backgroundColor: "#f0f0f0",
                                padding: 2,
                                borderRadius: 2,
                                fontSize: "14px",
                                overflowX: "auto",
                            }}
                        >
                            {JSON.stringify(data, null, 2)}
                        </Box>
                    ) : (
                        <Typography>Загрузка данных...</Typography>
                    )}
                </CardContent>
            </Card>
        </Container>
    );
}
