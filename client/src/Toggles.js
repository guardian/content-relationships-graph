import  React  from 'react';
export const Toggles = ({toggles, selected, handler})=>{
    const handle = (toggle) => (event) => { 
        handler && handler(toggle)
    }
    return(
<div className="toggles">
{toggles.map(toggle => (
    <div key={toggle}>
        <input type="checkbox" onChange={handle(toggle)} checked={toggle === selected} id={toggle}/>
        <label htmlFor={toggle}>{toggle}</label>
    </div>
))}
</div>)}