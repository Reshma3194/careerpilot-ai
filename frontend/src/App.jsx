import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar.jsx';
import Sidebar from './components/Sidebar.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Dashboard from './pages/Dashboard.jsx';
import ResumeAnalyzer from './pages/ResumeAnalyzer.jsx';
import JobTracker from './pages/JobTracker.jsx';
import JobDetails from './pages/JobDetails.jsx';
import InterviewPrep from './pages/InterviewPrep.jsx';
import Profile from './pages/Profile.jsx';

function AppLayout() {
  return (
    <div className="shell">
      <Navbar />
      <Sidebar />
      <main className="main">
        <Outlet />
      </main>
    </div>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<Dashboard />} />
        <Route path="/resume" element={<ResumeAnalyzer />} />
        <Route path="/jobs" element={<JobTracker />} />
        <Route path="/jobs/:id" element={<JobDetails />} />
        <Route path="/interview" element={<InterviewPrep />} />
        <Route path="/profile" element={<Profile />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
