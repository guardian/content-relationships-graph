import React, { useState, useEffect } from 'react';
import './App.css';

import { Card, More } from './card';
import { Toggles } from './Toggles';
console.log(Card)
const App = () => {
const [path,setPath] = useState("politics/2019/mar/25/mps-seize-control-of-brexit-process-by-backing-indicative-votes-amendment")  
return (
    <>
    {/* <input type="text" value={path} onChange={e=>setPath(e.target.value)}/> */}
  
    <>
      <Page path={path}  />
      Links from:
      <Links path={path}  changePath={setPath} />
      Links to:
      <Links path={path} to changePath={setPath}/>
      Atoms:
      <Atoms path={path}/>
      </>
    
    </>
  );
}

const Atoms = ({ path }) => {
  const [atoms, setAtoms] = useState([])
  useEffect(()=>{
    fetch(`http://localhost:8080/atom/${path}`).then(_ => _.json()).then(xs => {
    setAtoms(xs.reduce((o,atom, i)=>({...o,[`${atom.atomType} ${i}`]:`${atom.atomType}/${atom.atomId}`}),{}))
    })
  },[path])
  return (<Toggles toggles={atoms} handler={console.log}/>)
}

const Page = ({ path }) => {
  const [page, storePage] = useState(null)
  useEffect(
    () => {
      fetch(`http://localhost:8080/content/${path}`).then(_ => _.json()).then(page => {
      console.log(page)  
      storePage(page)
      }).catch(console.log)
    }, [path]
  )

  return (<>
    {page !== null &&
      <Card title={page.title} image={page.image} to={false} />
    }</>
  )
}

const Links = ({ path, to, changePath }) => {
  const [links, setLinks] = useState(null)
  const [counter, updateCount] = useState(0)
  const [refreshed, updateRefreshed] = useState([])
  useEffect(()=>{
    fetch(`http://localhost:8080/links${to?'in':'out'}/${path}`).then(_ => _.json()).then(ls =>{
    
       ls.map(link=>{
         if("Left" in link && refreshed.indexOf(link.Left.value[1]) === -1) {
           updateRefreshed(a=>  [...a,link.Left.value[1]])
         fetch(`http://localhost:8080/content/${link.Left.value[1]}`,{method:'POST',mode:'no-cors'})
         .then(_=> {
           console.log("LEFT!")
           console.log(`http://localhost:8080/content/${link.Left.value[1]}`)
           updateCount(_=>_+1)
          })
        }
       })
       setLinks(ls)
    }).catch(console.log)
  },[path, to, counter])
  if (links == null) return <p>loading</p>
  const pages = links.map(link=>{
    if('Right'in link) 
    return <Card 
    title={`${link.Right.value[0]}: ${link.Right.value[1].title}`} 
    image={link.Right.value[1].image}
     click={()=>changePath(link.Right.value[1].path)}/> 
    return <Card title={`${link.Left.value[0]}:${link.Left.value[1]}`} click={()=>changePath(link.Right.value[1])}/>
  })
  return (<>
    <div className="cards">
 {pages}
  </div>
  </>)
}


export default App;
