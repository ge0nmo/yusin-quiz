const answerDetail = document.querySelector('.answer-detail');
const answerForm = document.getElementById('answer-form');

const questionId = document.getElementById('questionId').value;

// Initialize - load answers on page load
window.onload = function() {
    answerForm.addEventListener('submit', handlerRegisterAnswer);

}


async function handlerRegisterAnswer(e){
    e.preventDefault();
    const content = document.getElementById('answer-content').value.trim();
    console.log(`answer content = `, content)

    if(!content){
        alert('답변 내용을 입력해주세요');
        return;
    }

    const request = {
        content: content
    };

    await postJSON(`/admin/question/${questionId}/answer`, request);

    resetAnswerForm(content);
    window.location.reload();
}


function resetAnswerForm(contentElement){
    contentElement.value = '';
}

const postJSON = async function(url, uploadData){
    return await sendJson('POST', url, uploadData);
}

const patchJSON = async function(url, uploadData){
    return await sendJson('PATCH', url, uploadData);
}


const sendJson  = async function(method, url, uploadData){
    try{
        const fetchPro = fetch(url, {
            method: method,
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

const getJSON = async function(url) {
    try {
        const res = await fetch(url);
        const data = await res.json();
        if (!res.ok) throw new Error(`${data.message} (${res.status})`);
        return data;
    } catch (err) {
        console.error('Error fetching data:', err);
        throw err;
    }
};