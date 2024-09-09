import React from 'react';

const products = [
    { id: 1, name: 'Product 1', price: 29.99 },
    { id: 2, name: 'Product 2', price: 49.99 },
    { id: 3, name: 'Product 3', price: 19.99 },
];

const ExchangeList: React.FC = () => {
    return (
        <div>
            <h1>Product List</h1>
            <ul>
                {products.map((product) => (
                    <li key={product.id}>
                        {product.name}: ${product.price}
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default ExchangeList;