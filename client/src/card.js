import React from 'react';

export const Card = ({ link, title, image, click }) => (
    <div className="card">
        <button href="#" onClick={() => { click ? click() : console.log(title) }}>

            {image && <img alt={title} src={image} />}
            <p className="link">{link}</p>
            <p className="title">{title}</p>
        </button>
    </div>)
export const More = ({ n }) => (
    <div className="more">
        <div>...</div>
        <div>{n} more</div>
    </div>
)