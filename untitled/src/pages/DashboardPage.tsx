// src/pages/DashboardPage.tsx
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

export default function DashboardPage() {
    const [data, setData] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem("token");
        if (!token) {
            navigate("/login");
        } else {
            const fetchData = async () => {
                try {
                    const response = await axios.get("http://localhost:8080/api/v1/protected-resource", {
                        headers: { Authorization: `Bearer ${token}` },
                    });
                    setData(response.data);
                } catch (error) {
                    console.error("Ошибка при получении данных", error);
                }
            };
            fetchData();
        }
    }, [navigate]);

    return (
        <div style={{ marginTop: "100px", textAlign: "center" }}>
            <h2>Добро пожаловать в дашборд!</h2>
            {data ? <pre>{JSON.stringify(data, null, 2)}</pre> : <p>Загрузка...</p>}
        </div>
    );
}
