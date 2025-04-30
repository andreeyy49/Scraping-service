import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import axios from "axios";
import {
    Container,
    Typography,
    CircularProgress,
    Accordion,
    AccordionSummary,
    AccordionDetails,
    Grid,
    Card,
    CardContent,
    CardMedia,
    Box,
    Pagination,
    Button,
    Link,
    AppBar,
    Toolbar,
    IconButton,
    Drawer,
    Stack,
    Divider
} from "@mui/material";
import {
    Menu as MenuIcon,
    Store as StoreIcon,
    Article as ArticleIcon,
    Logout as LogoutIcon,
    ExpandMore as ExpandMoreIcon,
    ArrowBack as ArrowBackIcon
} from "@mui/icons-material";

interface Product {
    id: string;
    name: string;
    price: string;
    imageUrl: string;
    productUrl: string;
}

interface PageData<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
}

interface ProductCostChange {
    product: Product;
    costChange: { [date: string]: string }; // ключ — дата, значение — цена
}

export default function SiteStatsPage() {
    const { siteId } = useParams();
    const navigate = useNavigate();
    const [newProducts, setNewProducts] = useState<PageData<Product> | null>(null);
    const [oldProducts, setOldProducts] = useState<PageData<Product> | null>(null);
    const [priceChangedProducts, setPriceChangedProducts] = useState<PageData<ProductCostChange> | null>(null);
    const [loading, setLoading] = useState(true);
    const [openDrawer, setOpenDrawer] = useState(false);
    const [expanded, setExpanded] = useState<string | false>(false);

    const [priceChangePage, setPriceChangePage] = useState(1);
    const [newPage, setNewPage] = useState(1);
    const [oldPage, setOldPage] = useState(1);
    const itemsPerPage = 9;

    const handleAccordionChange = (panel: string) => (_: React.SyntheticEvent, isExpanded: boolean) => {
        setExpanded(isExpanded ? panel : false);
    };

    useEffect(() => {
        const fetchStats = async () => {
            try {
                setLoading(true);
                const token = localStorage.getItem("token");

                // Всегда делаем запросы при первой загрузке или при изменении страницы
                const shouldFetchNew = !newProducts || newPage !== (newProducts?.number + 1);
                const shouldFetchOld = !oldProducts || oldPage !== (oldProducts?.number + 1);
                const shouldFetchPriceChanged = !priceChangedProducts || priceChangePage !== (priceChangedProducts?.number + 1);

                const requests = [];

                if (shouldFetchNew) {
                    requests.push(
                        axios.get(`http://localhost:8080/api/v1/entity-vault/product/findNew/${siteId}`, {
                            headers: { Authorization: `Bearer ${token}` },
                            params: { page: newPage - 1, size: itemsPerPage }
                        }).then(res => setNewProducts(res.data))
                    );
                }

                if (shouldFetchOld) {
                    requests.push(
                        axios.get(`http://localhost:8080/api/v1/entity-vault/product/findOld/${siteId}`, {
                            headers: { Authorization: `Bearer ${token}` },
                            params: { page: oldPage - 1, size: itemsPerPage }
                        }).then(res => setOldProducts(res.data))
                    );
                }

                if (shouldFetchPriceChanged) {
                    requests.push(
                        axios.get(`http://localhost:8080/api/v1/entity-vault/product/findCostProgress/${siteId}`, {
                            headers: { Authorization: `Bearer ${token}` },
                            params: { page: priceChangePage - 1, size: 4 }
                        }).then(res => setPriceChangedProducts(res.data))
                    );
                }

                await Promise.all(requests);
            } catch (err) {
                console.error("Ошибка при загрузке статистики", err);
            } finally {
                setLoading(false);
            }
        };

        fetchStats();
    }, [siteId, newPage, oldPage, priceChangePage]);


    const handleLogout = async () => {
        try {
            await fetch("/api/v1/auth/logout", {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${localStorage.getItem("token")}`,
                },
            });
        } catch (error) {
            console.error("Ошибка при выходе из системы", error);
        } finally {
            localStorage.removeItem("token");
            navigate("/login");
        }
    };

    if (loading && !newProducts && !oldProducts) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="100vh">
                <CircularProgress />
            </Box>
        );
    }

    return (
        <>
            <AppBar position="static" color="default" elevation={0} sx={{ bgcolor: 'white' }}>
                <Toolbar>
                    <IconButton edge="start" color="inherit" onClick={() => setOpenDrawer(true)} sx={{ mr: 2 }}>
                        <MenuIcon />
                    </IconButton>

                    <IconButton edge="start" color="inherit" onClick={() => navigate(-1)} sx={{ mr: 2 }}>
                        <ArrowBackIcon />
                    </IconButton>

                    <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                        Статистика по сайту #{siteId}
                    </Typography>

                    <Button
                        variant="outlined"
                        color="error"
                        onClick={handleLogout}
                        startIcon={<LogoutIcon />}
                        sx={{ ml: 2 }}
                    >
                        Выйти
                    </Button>
                </Toolbar>
            </AppBar>

            <Container sx={{ mt: 4 }}>
                <Accordion
                    expanded={expanded === 'panel1'}
                    onChange={handleAccordionChange('panel1')}
                    sx={{ mb: 3 }}
                >
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                            🆕 Новые товары ({newProducts?.totalElements ?? 0})
                        </Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        <ProductGrid
                            products={newProducts?.content ?? []}
                            page={newPage}
                            totalPages={newProducts?.totalPages ?? 1}
                            onPageChange={(_, val) => setNewPage(val)}
                            loading={loading}
                        />
                    </AccordionDetails>
                </Accordion>

                <Accordion
                    expanded={expanded === 'panel2'}
                    onChange={handleAccordionChange('panel2')}
                    sx={{ mb: 3 }}
                >
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                            📉 Пропавшие товары ({oldProducts?.totalElements ?? 0})
                        </Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        <ProductGrid
                            products={oldProducts?.content ?? []}
                            page={oldPage}
                            totalPages={oldProducts?.totalPages ?? 1}
                            onPageChange={(_, val) => setOldPage(val)}
                            loading={loading}
                        />
                    </AccordionDetails>
                </Accordion>

                <Accordion
                    expanded={expanded === 'panel3'}
                    onChange={handleAccordionChange('panel3')}
                >
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                            📊 Изменение цен ({priceChangedProducts?.totalElements ?? 0})
                        </Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                        <PriceChangeGrid
                            products={priceChangedProducts?.content ?? []}
                            page={priceChangePage}
                            totalPages={priceChangedProducts?.totalPages ?? 1}
                            onPageChange={(_, val) => setPriceChangePage(val)}
                            loading={loading}
                        />
                    </AccordionDetails>
                </Accordion>
            </Container>

            <Drawer
                anchor="left"
                open={openDrawer}
                onClose={() => setOpenDrawer(false)}
                sx={{
                    "& .MuiDrawer-paper": {
                        width: 250,
                        padding: 2,
                        paddingTop: 4,
                    },
                }}
            >
                <Stack spacing={2}>
                    <Typography variant="h6">
                        Навигация
                    </Typography>

                    <Button
                        variant="outlined"
                        startIcon={<StoreIcon />}
                        onClick={() => navigate("/dashboard")}
                        sx={{ justifyContent: 'flex-start' }}
                    >
                        Дашборд
                    </Button>

                    <Button
                        variant="contained"
                        startIcon={<StoreIcon />}
                        onClick={() => navigate("/analyze-shops")}
                        sx={{ justifyContent: 'flex-start' }}
                    >
                        Анализ магазинов
                    </Button>

                    <Button
                        variant="outlined"
                        startIcon={<ArticleIcon />}
                        onClick={() => navigate("/analyze-blogs")}
                        sx={{ justifyContent: 'flex-start' }}
                    >
                        Анализ статей
                    </Button>

                    <Divider sx={{ my: 2 }} />

                    <Button
                        variant="outlined"
                        color="error"
                        startIcon={<LogoutIcon />}
                        onClick={handleLogout}
                        sx={{ justifyContent: 'flex-start' }}
                    >
                        Выйти
                    </Button>
                </Stack>
            </Drawer>
        </>
    );
}

function ProductGrid({
                         products,
                         page,
                         totalPages,
                         onPageChange,
                         loading
                     }: {
    products: Product[],
    page: number,
    totalPages: number,
    onPageChange: (event: React.ChangeEvent<unknown>, value: number) => void,
    loading: boolean
}) {
    if (loading) {
        return (
            <Box display="flex" justifyContent="center" my={4}>
                <CircularProgress />
            </Box>
        );
    }

    if (products.length === 0) {
        return <Typography variant="body1" textAlign="center" sx={{ mt: 2 }}>Нет данных</Typography>;
    }

    return (
        <>
            <Box sx={{ display: 'flex', justifyContent: 'center', mb: 3 }}>
                <Pagination
                    count={totalPages}
                    page={page}
                    onChange={onPageChange}
                    color="primary"
                    showFirstButton
                    showLastButton
                />
            </Box>

            <Grid container spacing={3}>
                {products.map((product) => (
                    <Grid item xs={12} sm={6} md={4} key={product.id}>
                        <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                            <CardMedia
                                component="img"
                                height="200"
                                image={product.imageUrl || "https://via.placeholder.com/300"}
                                alt={product.name}
                                sx={{ objectFit: 'contain', p: 1 }}
                            />
                            <CardContent sx={{ flexGrow: 1 }}>
                                <Typography gutterBottom variant="h6" component="div">
                                    {product.name}
                                </Typography>
                                <Typography variant="h5" color="primary" sx={{ fontWeight: 'bold' }}>
                                    {product.price}
                                </Typography>
                            </CardContent>
                            <Box sx={{ p: 2 }}>
                                <Link
                                    href={product.productUrl}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    sx={{ textDecoration: 'none' }}
                                >
                                    <Button fullWidth variant="contained">
                                        Перейти к товару
                                    </Button>
                                </Link>
                            </Box>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </>
    );
}

function PriceChangeGrid({
    products,
    page,
    totalPages,
    onPageChange,
    loading
}: {
    products: ProductCostChange[],
        page: number,
        totalPages: number,
        onPageChange: (event: React.ChangeEvent<unknown>, value: number) => void,
        loading: boolean
}) {
    if (loading) return <Box display="flex" justifyContent="center" my={4}><CircularProgress /></Box>;
    if (products.length === 0) return <Typography textAlign="center">Нет данных</Typography>;

    return (
        <>
            <Box sx={{ display: 'flex', justifyContent: 'center', mb: 3 }}>
                <Pagination
                    count={totalPages}
                    page={page}
                    onChange={onPageChange}
                    color="primary"
                    showFirstButton
                    showLastButton
                />
            </Box>

            <Grid container spacing={3}>
                {products.map(({ product, costChange }) => {
                    const chartData = Object.entries(costChange)
                        .map(([date, price]) => ({ date, price: parseFloat(price.replace(/[^\d.]/g, '')) }));

                    return (
                        <Grid item xs={12} md={12} key={product.id}>
                            <Card sx={{ p: 2, width: '300px', height: '400px' }}>
                                <Grid container spacing={2} style={{ height: '100%' }}>
                                    <Grid item xs={4}>
                                        <img
                                            src={product.imageUrl || "https://via.placeholder.com/300"}
                                            alt={product.name}
                                            style={{ width: '100%', objectFit: 'contain' }}
                                        />
                                        <Typography variant="subtitle1" sx={{ mt: 1 }}>
                                            {product.name}
                                        </Typography>
                                        <Link href={product.productUrl} target="_blank">
                                            <Button fullWidth size="small">Открыть</Button>
                                        </Link>
                                    </Grid>
                                    <Grid item xs={8} style={{ height: '100%' }}>
                                        <ResponsiveContainer width="100%" height="100%">
                                            <LineChart data={chartData}>
                                                <CartesianGrid strokeDasharray="3 3" />
                                                <XAxis dataKey="date" tick={{ fontSize: 10 }} />
                                                <YAxis />
                                                <Tooltip />
                                                <Line type="monotone" dataKey="price" stroke="#8884d8" />
                                            </LineChart>
                                        </ResponsiveContainer>
                                    </Grid>
                                </Grid>
                            </Card>
                        </Grid>
                    );
                })}
            </Grid>
        </>
    );
}
