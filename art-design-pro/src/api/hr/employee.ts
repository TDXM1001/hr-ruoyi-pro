import request from '@/utils/http';

export function listEmployee(query: any) {
  return request.get<any>({
    url: '/hr/employee/list',
    params: query,
    returnFullResponse: true
  });
}

export function getEmployee(employeeId: number | string) {
  return request.get<any>({
    url: '/hr/employee/' + employeeId,
    returnFullResponse: true
  });
}

export function addEmployee(data: any) {
  return request.post<any>({
    url: '/hr/employee',
    data: data,
    returnFullResponse: true
  });
}

export function updateEmployee(data: any) {
  return request.put<any>({
    url: '/hr/employee',
    data: data,
    returnFullResponse: true
  });
}

export function delEmployee(employeeId: number | string) {
  return request.del<any>({
    url: '/hr/employee/' + employeeId,
    returnFullResponse: true
  });
}
