import React from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Search, Sparkles, Shield, Clock, Star, MapPin, Users } from 'lucide-react';
import SearchBar from '../components/SearchBar';
import PropertyCard from '../components/PropertyCard';

const features = [
  { icon: Shield, title: 'Secure Booking', desc: 'Safe & encrypted payments' },
  { icon: Star, title: 'Premium Stays', desc: 'Curated luxury properties' },
  { icon: Clock, title: '24/7 Support', desc: 'Round-the-clock assistance' },
  { icon: Sparkles, title: 'Best Rates', desc: 'Price match guarantee' },
];

const sampleProperties = [
  { name: 'Sea View Villa', city: 'Goa', state: 'Goa', averageRating: 4.8, slug: 'sea-view-villa', shortDescription: 'Luxurious beachfront villa with private pool', starCategory: 5, startingPrice: 12000 },
  { name: 'Mountain Retreat', city: 'Manali', state: 'Himachal Pradesh', averageRating: 4.9, slug: 'mountain-retreat', shortDescription: 'Cozy cottage with stunning Himalayan views', starCategory: 4, startingPrice: 8500 },
  { name: 'Coffee Plantation Stay', city: 'Coorg', state: 'Karnataka', averageRating: 4.7, slug: 'coffee-plantation', shortDescription: 'Heritage bungalow amidst coffee estates', starCategory: 4, startingPrice: 6500 },
  { name: 'Desert Camp', city: 'Jaisalmer', state: 'Rajasthan', averageRating: 4.6, slug: 'desert-camp', shortDescription: 'Luxury tented camp under the stars', starCategory: 5, startingPrice: 15000 },
];

export default function Landing() {
  return (
    <div>
      {/* Hero */}
      <section className="relative min-h-[90vh] hero-gradient flex items-center">
        <div className="absolute inset-0 bg-[url('https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=1920')] bg-cover bg-center opacity-20" />
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-32 md:py-40">
          <motion.div initial={{ opacity: 0, y: 30 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.8 }} className="max-w-3xl">
            <h1 className="text-4xl md:text-6xl lg:text-7xl font-display font-bold text-white leading-tight mb-6">
              Find Your Perfect<br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-amber-300 to-yellow-200">Luxury Escape</span>
            </h1>
            <p className="text-lg md:text-xl text-white/80 mb-10 max-w-xl">
              Discover hand-picked luxury homestays across India's most breathtaking destinations.
            </p>
            <SearchBar />
            <div className="flex items-center gap-6 mt-8 text-white/70 text-sm">
              <span className="flex items-center gap-1"><Star className="w-4 h-4 text-yellow-400" /> 500+ Properties</span>
              <span className="flex items-center gap-1"><MapPin className="w-4 h-4 text-primary-300" /> 50+ Destinations</span>
              <span className="flex items-center gap-1"><Users className="w-4 h-4 text-primary-300" /> 10K+ Happy Guests</span>
            </div>
          </motion.div>
        </div>
      </section>

      {/* Features */}
      <section className="py-16 md:py-20 -mt-16 relative z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {features.map((f, i) => (
              <motion.div key={i} initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.1 + 0.3 }}
                className="bg-white rounded-2xl p-6 shadow-lg border border-gray-100 text-center card-hover">
                <div className="w-12 h-12 rounded-full bg-primary-50 flex items-center justify-center mx-auto mb-3">
                  <f.icon className="w-6 h-6 text-primary-600" />
                </div>
                <h3 className="font-semibold text-gray-900 mb-1">{f.title}</h3>
                <p className="text-sm text-gray-500">{f.desc}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Featured Properties */}
      <section className="py-16 md:py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-end justify-between mb-10">
            <div>
              <h2 className="text-3xl md:text-4xl font-display font-bold text-gray-900">Featured Stays</h2>
              <p className="text-gray-500 mt-2">Hand-picked properties for your next getaway</p>
            </div>
            <Link to="/search" className="text-primary-600 font-medium hover:text-primary-700 transition-colors text-sm">
              View all &rarr;
            </Link>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {sampleProperties.map((p, i) => (
              <motion.div key={p.slug} initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.1 }}>
                <PropertyCard property={p} />
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-20 hero-gradient">
        <div className="max-w-4xl mx-auto px-4 text-center">
          <h2 className="text-3xl md:text-4xl font-display font-bold text-white mb-4">
            Ready to Experience Luxury?
          </h2>
          <p className="text-white/80 mb-8 max-w-xl mx-auto">
            Join thousands of travelers who've found their perfect stay with DriftStay.
          </p>
          <Link to="/register"
            className="inline-flex items-center gap-2 bg-white text-primary-700 font-semibold px-8 py-4 rounded-full hover:bg-gray-100 transition-colors shadow-xl">
            <Sparkles className="w-5 h-5" /> Get Started
          </Link>
        </div>
      </section>
    </div>
  );
}

