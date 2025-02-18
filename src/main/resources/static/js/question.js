let currentPage = 1;
const pageSize = 10;
const prevPage = document.querySelector('.prevPage');
const nextPage = document.querySelector('.nextPage');
const questionList = document.querySelector('#questionList');

window.onload = function(){
    loadPage();

    // Add event listeners for pagination
    prevPage.addEventListener('click', handlePrevPage);
    nextPage.addEventListener('click', handleNextPage);
};

async function loadPage(){
  try {
      const data = await getJSON(`/admin/question/list?page=${currentPage - 1}&size=${pageSize}`);
      renderQuestionList(data.data);

      // Update pagination buttons based on pageInfo
      updatePaginationButtons(data.pageInfo);
  } catch (err) {
      console.error('Error loading page:', err);
  }
}

function renderQuestionList(questions) {
    // Clear existing content
    questionList.innerHTML = '';

    if (!questions || questions.length === 0) {
        questionList.innerHTML = '<tr><td colspan="3" class="text-center">등록된 질문이 없습니다.</td></tr>';
        return;
    }

  // Create rows for each question
    questions.forEach(question => {

        console.log('question = ', question);
        console.log("답글 달렸나? = ", question.answeredByAdmin);
      const row = document.createElement('tr');

    // Add 'table-success' class if question is answered
      if (question.answeredByAdmin) {
        row.classList.add('table-success');
      }

      row.innerHTML = `
        <td>${question.username}</td>
        <td>
            <a href="/admin/question/${question.id}/answer">
                ${question.title}
            </a>
        </td>
        <td>${formatDate(question.createdAt)}</td>
      `;

      questionList.appendChild(row);
  });
}

function formatDate(dateString) {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}`;
}

function updatePaginationButtons(pageInfo) {
    // Disable previous page button if on first page
    if (pageInfo.currentPage <= 1) {
        prevPage.parentElement.classList.add('disabled');
    } else {
        prevPage.parentElement.classList.remove('disabled');
    }

    // Disable next page button if on last page
    if (pageInfo.currentPage >= pageInfo.totalPages) {
        nextPage.parentElement.classList.add('disabled');
    } else {
        nextPage.parentElement.classList.remove('disabled');
    }
}

function handlePrevPage(e){
    e.preventDefault();
    if (currentPage > 1) {
        currentPage--;
        loadPage();
    }
}

function handleNextPage(e){
    e.preventDefault();
    if (currentPage < document.querySelector('.nextPage').parentElement.classList.contains('disabled')) {
        return;
    }
    currentPage++;
    loadPage();
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