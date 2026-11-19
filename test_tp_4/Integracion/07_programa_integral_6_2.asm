.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    u_main_basea DD ?
    v_main_baseb DQ ?
    w_main_derivada DD ?
    s1_basea_u_main DD ?
    s1_baseb_v_main DQ ?
    s1_w_main DD ?
    s2_basea_u_main DD ?
    s2_baseb_v_main DQ ?
    s2_w_main DD ?
    i_main DD ?
    total_main DD ?
    c_4_ul_ul DD 4
    c_1_25 DQ 1.25
    c_9_ul_ul DD 9
    c_1_ul_ul DD 1
    c_0_ul_ul DD 0
    c_5_ul_ul DD 5
    total DB "total=",0
    @aux18 DD ?
    @aux20 DD ?
    @aux23 DD ?
    @aux25 DD ?
    @aux32 DD ?
    fmt_ulongint DB "%u",0
    fmt_doublef DB "%f",0
    fmt_cadena DB "%s",0
    msg_overflow_suma DB "Error: overflow en suma de ulongint",0

.code
start:
fn_identidad_main:
    push ebp
    mov ebp, esp
    mov eax, dword ptr [ebp+8]
    leave
    ret
fin_identidad_main:
    leave
    ret
    mov eax, c_4_ul_ul
    mov s1_basea_u_main, eax
    fld c_1_25
    fstp s1_baseb_v_main
    mov eax, c_9_ul_ul
    mov s1_w_main, eax
    mov eax, s1_basea_u_main
    mov s2_basea_u_main, eax
    mov eax, dword ptr [s1_baseb_v_main+4]
    mov dword ptr [s2_baseb_v_main+4], eax
    mov eax, dword ptr [s1_baseb_v_main]
    mov dword ptr [s2_baseb_v_main], eax
    mov eax, s1_w_main
    mov s2_w_main, eax
    mov eax, c_1_ul_ul
    mov i_main, eax
    mov eax, c_0_ul_ul
    mov total_main, eax
label_1:
    mov eax, i_main
    cmp eax, c_4_ul_ul
    setbe al
    movzx eax, al
    mov @aux18, eax
    mov eax, i_main
    cmp eax, c_4_ul_ul
    ja label_2
    mov eax, total_main
    add eax, i_main
    jc error_overflow_suma
    mov @aux20, eax
    mov eax, @aux20
    mov total_main, eax
label_2:
    mov eax, i_main
    add eax, c_1_ul_ul
    jc error_overflow_suma
    mov @aux23, eax
    mov eax, @aux23
    mov i_main, eax
    mov eax, i_main
    cmp eax, c_5_ul_ul
    setbe al
    movzx eax, al
    mov @aux25, eax
    mov eax, i_main
    cmp eax, c_5_ul_ul
    ja label_3
    jmp label_1
label_3:
    invoke crt_printf, offset fmt_cadena, offset total
    invoke crt_printf, offset fmt_ulongint, total_main
    ; (PAR, n:main:identidad, 5$ul)
    sub esp, 4
    mov eax, c_5_ul_ul
    mov dword ptr [esp+0], eax
    call fn_identidad_main
    add esp, 4
    mov @aux32, eax
    invoke crt_printf, offset fmt_ulongint, @aux32
    invoke crt_printf, offset fmt_ulongint, s2_basea_u_main
    invoke crt_printf, offset fmt_doublef, s2_baseb_v_main
    invoke crt_printf, offset fmt_ulongint, s2_w_main
    jmp fin_normal
error_overflow_suma:
    invoke crt_printf, offset msg_overflow_suma
    invoke ExitProcess, 1
fin_normal:
    invoke ExitProcess, 0
end start
