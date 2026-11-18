.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    c_1_ul_ul DD 1
    x_main DD ?
    c_4_ul_ul DD 4
    @aux3 DD ?
    @aux5 DQ ?
    @aux10 DD ?

.code
start:
fn_mezcla_main:
    push ebp
    mov ebp, esp
    sub esp, 16
    mov eax, dword ptr [ebp+8]
    add eax, c_1_ul_ul
    mov @aux3, eax
    mov eax, @aux3
    mov dword ptr [ebp-4], eax
    fild dword ptr [ebp-4]
    fstp @aux5
    fld @aux5
    fstp qword ptr [ebp-16]
    mov eax, dword ptr [ebp-4]
    leave
    ret
fin_mezcla_main:
    leave
    ret
    ; (PAR, a:main:mezcla, 4$ul)
    sub esp, 4
    mov eax, c_4_ul_ul
    mov dword ptr [esp+0], eax
    call fn_mezcla_main
    add esp, 4
    mov @aux10, eax
    mov eax, @aux10
    mov x_main, eax
    invoke ExitProcess, 0
end start
