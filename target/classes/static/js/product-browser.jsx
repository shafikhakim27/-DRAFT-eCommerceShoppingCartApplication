// React Component for Product Browser using REST API

const { useState, useEffect } = React;

function ProductBrowser() {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [selectedCategory, setSelectedCategory] = useState('');
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Fetch products from REST API
    const fetchProducts = async (category = '', search = '') => {
        setLoading(true);
        try {
            let url = '/api/products';
            const params = new URLSearchParams();
            
            if (category) params.append('category', category);
            if (search) params.append('search', search);
            
            if (params.toString()) {
                url += '?' + params.toString();
            }
            
            const response = await fetch(url);
            if (!response.ok) throw new Error('Failed to fetch products');
            
            const data = await response.json();
            setProducts(data);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    // Fetch categories
    const fetchCategories = async () => {
        try {
            const response = await fetch('/api/products/categories');
            if (!response.ok) throw new Error('Failed to fetch categories');
            const data = await response.json();
            setCategories(data);
        } catch (err) {
            console.error('Error fetching categories:', err);
        }
    };

    // Initial load
    useEffect(() => {
        fetchProducts();
        fetchCategories();
    }, []);

    // Handle category change
    const handleCategoryChange = (category) => {
        setSelectedCategory(category);
        setSearchTerm('');
        fetchProducts(category, '');
    };

    // Handle search
    const handleSearch = (e) => {
        e.preventDefault();
        setSelectedCategory('');
        fetchProducts('', searchTerm);
    };

    // Format price
    const formatPrice = (price) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        }).format(price);
    };

    if (loading) {
        return (
            <div className="text-center py-4">
                <i className="fas fa-spinner fa-spin fa-2x text-primary"></i>
                <p className="mt-2">Loading products...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="alert alert-danger" role="alert">
                <i className="fas fa-exclamation-triangle me-2"></i>
                Error: {error}
            </div>
        );
    }

    return (
        <div className="react-product-browser">
            <div className="card">
                <div className="card-header">
                    <h5 className="mb-0">
                        <i className="fab fa-react me-2 text-info"></i>
                        Product Browser (React + REST API)
                    </h5>
                </div>
                <div className="card-body">
                    {/* Search and Filter Controls */}
                    <div className="row mb-4">
                        <div className="col-md-6">
                            <form onSubmit={handleSearch}>
                                <div className="input-group">
                                    <input
                                        type="text"
                                        className="form-control"
                                        placeholder="Search products..."
                                        value={searchTerm}
                                        onChange={(e) => setSearchTerm(e.target.value)}
                                    />
                                    <button className="btn btn-outline-primary" type="submit">
                                        <i className="fas fa-search"></i>
                                    </button>
                                </div>
                            </form>
                        </div>
                        <div className="col-md-6">
                            <select 
                                className="form-select"
                                value={selectedCategory}
                                onChange={(e) => handleCategoryChange(e.target.value)}
                            >
                                <option value="">All Categories</option>
                                {categories.map(category => (
                                    <option key={category} value={category}>
                                        {category}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>

                    {/* Products Grid */}
                    <div className="row">
                        {products.length === 0 ? (
                            <div className="col-12 text-center py-4">
                                <i className="fas fa-search fa-2x text-muted mb-2"></i>
                                <h5 className="text-muted">No products found</h5>
                                <p className="text-muted">Try adjusting your search or filter criteria.</p>
                            </div>
                        ) : (
                            products.map(product => (
                                <div key={product.id} className="col-lg-4 col-md-6 mb-3">
                                    <div className="react-product-card">
                                        <div className="d-flex justify-content-between align-items-start mb-2">
                                            <h6 className="mb-1">{product.name}</h6>
                                            <span className="badge bg-secondary">{product.category}</span>
                                        </div>
                                        <p className="text-muted small mb-2">
                                            {product.description?.substring(0, 100)}
                                            {product.description?.length > 100 ? '...' : ''}
                                        </p>
                                        <div className="d-flex justify-content-between align-items-center">
                                            <span className="price-tag">{formatPrice(product.price)}</span>
                                            <span className={`stock-info ${product.stockQuantity === 0 ? 'out-of-stock' : ''}`}>
                                                {product.stockQuantity === 0 ? 'Out of Stock' : 
                                                 `${product.stockQuantity} in stock`}
                                            </span>
                                        </div>
                                        <div className="mt-2">
                                            <a 
                                                href={`/products/${product.id}`} 
                                                className="btn btn-sm btn-outline-primary"
                                            >
                                                <i className="fas fa-eye me-1"></i>View Details
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>

                    {/* Product Count */}
                    <div className="text-center mt-3">
                        <small className="text-muted">
                            Showing {products.length} product{products.length !== 1 ? 's' : ''}
                            {selectedCategory && ` in ${selectedCategory}`}
                            {searchTerm && ` matching "${searchTerm}"`}
                        </small>
                    </div>
                </div>
            </div>
        </div>
    );
}

// Render the component
const domContainer = document.querySelector('#product-browser-react');
if (domContainer) {
    ReactDOM.render(React.createElement(ProductBrowser), domContainer);
}