import{ getJSON } from "../../helper.js";
import { SUBJECT_URL } from "../subjectConfig.js";

export const state = {
    data: {},
    page:{
        currentPage: 1,
        pageSize: 10,
        totalElement: 0,
        totalPages: 0,
    }

}

export const loadSubject = async function(){
    const response = await getJSON(`${SUBJECT_URL}?page=${state.page.currentPage}&size=${state.page.pageSize}`);
    console.log('데이터 = ', response);

    state.data  = response.data;
    state.page = response.pageInfo;

    console.log("state= ", state);
}
