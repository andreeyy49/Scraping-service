import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
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

export default function SiteStatsPage() {
    const { siteId } = useParams();
    const navigate = useNavigate();
    const [newProducts, setNewProducts] = useState<PageData<Product> | null>(null);
    const [oldProducts, setOldProducts] = useState<PageData<Product> | null>(null);
    const [loading, setLoading] = useState(true);
    const [openDrawer, setOpenDrawer] = useState(false);
    const [expanded, setExpanded] = useState<string | false>(false);

    const [newPage, setNewPage] = useState(1);
    const [oldPage, setOldPage] = useState(1);
    const itemsPerPage = 9;

    const handleAccordionChange = (panel: string) => (event: React.SyntheticEvent, isExpanded: boolean) => {
        setExpanded(isExpanded ? panel : false);
    };

    useEffect(() => {
        const fetchStats = async () => {
            try {
                setLoading(true);
                const token = localStorage.getItem("token");

                const [newRes, oldRes] = await Promise.all([
                    axios.get(`http://localhost:8080/api/v1/entity-vault/product/findNew/${siteId}`, {
                        headers: { Authorization: `Bearer ${token}` },
                        params: { page: newPage - 1, size: itemsPerPage }
                    }),
                    axios.get(`http://localhost:8080/api/v1/entity-vault/product/findOld/${siteId}`, {
                        headers: { Authorization: `Bearer ${token}` },
                        params: { page: oldPage - 1, size: itemsPerPage }
                    })
                ]);

                setNewProducts(newRes.data);
                setOldProducts(oldRes.data);
            } catch (err) {
                console.error("Ошибка при загрузке статистики", err);
            } finally {
                setLoading(false);
            }
        };

        fetchStats();
    }, [siteId, newPage, oldPage]);

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
                    <IconButton
                        edge="start"
                        color="inherit"
                        onClick={() => setOpenDrawer(true)}
                        sx={{ mr: 2 }}
                    >
                        <MenuIcon />
                    </IconButton>

                    <IconButton
                        edge="start"
                        color="inherit"
                        onClick={() => navigate(-1)}
                        sx={{ mr: 2 }}
                    >
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

            {/*{totalPages > 1 && (*/}
            {/*    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>*/}
            {/*        <Pagination*/}
            {/*            count={totalPages}*/}
            {/*            page={page}*/}
            {/*            onChange={onPageChange}*/}
            {/*            color="primary"*/}
            {/*            showFirstButton*/}
            {/*            showLastButton*/}
            {/*        />*/}
            {/*    </Box>*/}
            {/*)}*/}
        </>
    );
}