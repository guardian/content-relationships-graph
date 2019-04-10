import React from 'react';

export const Card = ({title,image}) => (
<div className="card">
    <button href="#">
    <img alt={title} src={image}/>
    {title}
    </button>
</div>)
export const More = ({n})=>(
    <div className="more">
    <div>...</div>
    <div>{n} more</div>
</div>
)