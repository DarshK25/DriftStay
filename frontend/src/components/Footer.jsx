import React from 'react';
import { Link } from 'react-router-dom';
import { Home, Mail, Phone, MapPin } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="bg-gray-900 text-gray-300">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Brand */}
          <div className="col-span-1 md:col-span-1">
            <Link to="/" className="flex items-center gap-2 mb-4">
              <Home className="w-6 h-6 text-primary-400" />
              <span className="font-display text-xl font-bold text-white">DriftStay</span>
            </Link>
            <p className="text-sm text-gray-400 leading-relaxed">
              Luxury homestay booking platform. Experience the finest stays across India's most beautiful destinations.
            </p>
          </div>

          {/* Quick Links */}
          <div>
            <h4 className="font-semibold text-white mb-4">Quick Links</h4>
            <div className="space-y-2">
              <Link to="/search" className="block text-sm hover:text-white transition-colors">Browse Stays</Link>
              <Link to="/search?city=Goa" className="block text-sm hover:text-white transition-colors">Goa</Link>
              <Link to="/search?city=Manali" className="block text-sm hover:text-white transition-colors">Manali</Link>
              <Link to="/search?city=Coorg" className="block text-sm hover:text-white transition-colors">Coorg</Link>
            </div>
          </div>

          {/* Support */}
          <div>
            <h4 className="font-semibold text-white mb-4">Support</h4>
            <div className="space-y-2">
              <Link to="#" className="block text-sm hover:text-white transition-colors">Help Center</Link>
              <Link to="#" className="block text-sm hover:text-white transition-colors">Cancellation Policy</Link>
              <Link to="#" className="block text-sm hover:text-white transition-colors">Safety Information</Link>
              <Link to="#" className="block text-sm hover:text-white transition-colors">Terms & Conditions</Link>
            </div>
          </div>

          {/* Contact */}
          <div>
            <h4 className="font-semibold text-white mb-4">Contact</h4>
            <div className="space-y-3 text-sm">
              <div className="flex items-center gap-2">
                <Mail className="w-4 h-4 text-primary-400" />
                <span>hello@driftstay.com</span>
              </div>
              <div className="flex items-center gap-2">
                <Phone className="w-4 h-4 text-primary-400" />
                <span>+91 1800-DRIFT</span>
              </div>
              <div className="flex items-start gap-2">
                <MapPin className="w-4 h-4 text-primary-400 mt-0.5" />
                <span>Mumbai, Maharashtra, India</span>
              </div>
            </div>
          </div>
        </div>

        <div className="border-t border-gray-800 mt-8 pt-8 text-center text-sm text-gray-500">
          <p>&copy; {new Date().getFullYear()} DriftStay. All rights reserved. Made with ❤️ in India.</p>
        </div>
      </div>
    </footer>
  );
}
