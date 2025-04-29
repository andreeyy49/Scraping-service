import {BrowserRouter, Routes, Route, Navigate} from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import RegisterPage from "./pages/RegisterPage";
import AddSitePage from "./pages/AddSitePage";
import SiteDetailsPage from "./pages/SiteDetailsPage";
import AnalyzeShopsPage from "./pages/AnalyzeShopsPage";
import AnalyzeBlogsPage from "./pages/AnalyzeBlogsPage";
import SiteStatsPage from "./pages/SiteStatsPage";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Navigate to="/login"/>}/>
                <Route path="/login" element={<LoginPage/>}/>
                <Route path="/register" element={<RegisterPage/>}/>
                <Route path="/dashboard" element={<DashboardPage/>}/>
                <Route path="/" element={<DashboardPage/>}/>
                <Route path="/analyze-shops" element={<AnalyzeShopsPage/>}/>
                <Route path="/analyze-blogs" element={<AnalyzeBlogsPage/>}/>
                <Route path="/add-site" element={<AddSitePage/>}/>
                <Route path="/site/:id" element={<SiteDetailsPage/>}/>
                <Route path="/site-stats/:siteId" element={<SiteStatsPage />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
