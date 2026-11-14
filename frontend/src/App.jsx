import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import AppRoutes from './routes/AppRoutes';
import Toast from './components/Toast';

export function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Toast />
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
