export const getJSON  = async function(url){
    try{
        const res = await fetch(url);
        const data = await res.json();

        if (!res.ok) {
            throw new Error(`${data.message} (${res.status})`);
        }
        return data;
    } catch (err){
        throw err
    }
}

export const postJSON = async function(url, uploadData){
    return await sendJson('POST', url, uploadData);
}

export const patchJSON = async function(url, uploadData){
    return await sendJson('PATCH', url, uploadData);
}


export const sendJson  = async function(method, url, uploadData){
    try{
        const fetchPro = fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(uploadData),
        });

        const res = await fetchPro;
        const data = await res.json();

        if (!res.ok) {
            throw new Error(`${data.message} (${res.status})`);
        }
        return data;
    } catch (err){
        throw err
    }
}