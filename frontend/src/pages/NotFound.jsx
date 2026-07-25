import React from 'react';
import { Link } from 'react-router-dom';
import { Home } from 'lucide-react';

export default function NotFound() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center px-4">
        <h1 className="text-8xl font-display font-bold gradient-text mb-4">404</h1>
        <h2 className="text-2xl font-semibold text-gray-900 mb-2">Page not found</h2>
        <p className="text-gray-500 mb-8 max-w-md mx-auto">
          The page you're looking for doesn't exist or has been moved.
        </p>
        <Link to="/"
          className="inline-flex items-center gap-2 bg-primary-600 text-white px-6 py-3 rounded-full font-medium hover:bg-primary-700 transition-colors">
          <Home className="w-5 h-5" /> Go home
        </Link>
      </div>
    </div>
  );
}
