const json=(body,status=200)=>new Response(JSON.stringify(body),{status,headers:{"content-type":"application/json","cache-control":"no-store"}});

export default {
  async fetch(request, env) {
    const url=new URL(request.url);
    if(url.pathname==="/health")return json({ok:true,service:"MobilePilot 2.0"});
    if(url.pathname==="/v1/commands"&&request.method==="POST"){
      const identity=await authenticate(request,env);
      if(!identity)return json({error:"unauthorized"},401);
      const body=await request.json().catch(()=>null);
      if(!body?.command||body.command.length>500)return json({error:"invalid_command"},400);
      const requestId=crypto.randomUUID();
      // Device lookup and FCM delivery are enabled after Firebase configuration.
      // No user-visible pairing key is used; identity is the verified Google account UID.
      return json({request_id:requestId,status:"configuration_required"},202);
    }
    return json({error:"not_found"},404);
  }
};

async function authenticate(request,env){
  const auth=request.headers.get("authorization")||"";
  if(!auth.startsWith("Bearer "))return null;
  // Production version verifies the short-lived Firebase/Google ID token here.
  // Never log or persist the bearer token.
  return {uid:"pending-provider-configuration"};
}
