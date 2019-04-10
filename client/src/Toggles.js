import  React  from 'react';
export const Toggles = ({toggles, handler})=>{
    const handle = (toggle) => (event) => { 
        console.log(toggle, event.target.checked)
    }
    return(
<div className="toggles">
{Object.keys(toggles).map(toggle => (
    <div key={toggle}>
        <input type="checkbox" onChange={handle(toggle)} checked={toggles[toggle]} id={toggle}/>
        <label htmlFor={toggle}>{toggle}</label>
    </div>
))}
</div>)}