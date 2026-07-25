import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Search, Menu, X, User, LogOut, Heart, Calendar, Home } from 'lucide-react';
import useAuthStore from '../store/authStore';
import { cn } from '../lib/utils';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuthStore();
  const [isOpen, setIsOpen] = React.useState(false);
  const [scrolled, setScrolled] = React.useState(false);
  const navigate = useNavigate();

  React.useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', onScroll);
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  return (
    <header className={cn(
      'fixed top-0 left-0 right-0 z-50 transition-all duration-300',
      scrolled ? 'bg-white/95 backdrop-blur-md shadow-sm' : 'bg-transparent'
    )}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16 md:h-20">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2">
            <Home className="w-6 h-6 text-primary-600" />
            <span className="font-display text-xl font-bold gradient-text">DriftStay</span>
          </Link>

          {/* Desktop Nav */}
          <nav className="hidden md:flex items-center gap-8">
            <Link to="/search" className="text-sm font-medium text-gray-700 hover:text-primary-600 transition-colors">
              Browse Stays
            </Link>
            {isAuthenticated ? (
              <>
                <Link to="/trips" className="text-sm font-medium text-gray-700 hover:text-primary-600 transition-colors flex items-center gap-1">
                  <Calendar className="w-4 h-4" /> My Trips
                </Link>
                <Link to="/wishlist" className="text-sm font-medium text-gray-700 hover:text-primary-600 transition-colors flex items-center gap-1">
                  <Heart className="w-4 h-4" /> Wishlist
                </Link>
                <div className="relative group">
                  <button className="flex items-center gap-2 px-3 py-2 rounded-full border border-gray-200 hover:shadow-md transition-shadow">
                    <Menu className="w-4 h-4" />
                    <div className="w-7 h-7 rounded-full bg-primary-600 flex items-center justify-center">
                      <span className="text-xs font-semibold text-white">
                        {user?.firstName?.[0]}{user?.lastName?.[0]}
                      </span>
                    </div>
                  </button>
                  <div className="absolute right-0 mt-2 w-56 bg-white rounded-xl shadow-xl border border-gray-100 py-2 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200">
                    <div className="px-4 py-2 border-b border-gray-100">
                      <p className="text-sm font-medium">{user?.firstName} {user?.lastName}</p>
                      <p className="text-xs text-gray-500">{user?.email}</p>
                    </div>
                    <Link to="/profile" className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">Profile</Link>
                    <Link to="/trips" className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">My Trips</Link>
                    <Link to="/wishlist" className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">Wishlist</Link>
                    <Link to="/host" className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">Host Dashboard</Link>
                    <div className="border-t border-gray-100 mt-1 pt-1">
                      <button onClick={handleLogout} className="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 flex items-center gap-2">
                        <LogOut className="w-4 h-4" /> Log out
                      </button>
                    </div>
                  </div>
                </div>
              </>
            ) : (
              <div className="flex items-center gap-3">
                <Link to="/login" className="text-sm font-medium text-gray-700 hover:text-primary-600 transition-colors">
                  Log in
                </Link>
                <Link to="/register" className="text-sm font-semibold text-white bg-primary-600 hover:bg-primary-700 px-5 py-2.5 rounded-full transition-colors">
                  Sign up
                </Link>
              </div>
            )}
          </nav>

          {/* Mobile menu button */}
          <button onClick={() => setIsOpen(!isOpen)} className="md:hidden p-2">
            {isOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>

        {/* Mobile Nav */}
        {isOpen && (
          <div className="md:hidden pb-4 border-t border-gray-100 animate-in">
            <div className="flex flex-col gap-2 pt-4">
              <Link to="/search" className="px-4 py-2 text-sm hover:bg-gray-50 rounded-lg" onClick={() => setIsOpen(false)}>Browse Stays</Link>
              {isAuthenticated ? (
                <>
                  <Link to="/trips" className="px-4 py-2 text-sm hover:bg-gray-50 rounded-lg" onClick={() => setIsOpen(false)}>My Trips</Link>
                  <Link to="/wishlist" className="px-4 py-2 text-sm hover:bg-gray-50 rounded-lg" onClick={() => setIsOpen(false)}>Wishlist</Link>
                  <Link to="/profile" className="px-4 py-2 text-sm hover:bg-gray-50 rounded-lg" onClick={() => setIsOpen(false)}>Profile</Link>
                  <button onClick={handleLogout} className="px-4 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg text-left">Log out</button>
                </>
              ) : (
                <div className="flex gap-2 px-4">
                  <Link to="/login" className="flex-1 text-center py-2 text-sm border border-gray-300 rounded-full" onClick={() => setIsOpen(false)}>Log in</Link>
                  <Link to="/register" className="flex-1 text-center py-2 text-sm bg-primary-600 text-white rounded-full" onClick={() => setIsOpen(false)}>Sign up</Link>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </header>
  );
}
