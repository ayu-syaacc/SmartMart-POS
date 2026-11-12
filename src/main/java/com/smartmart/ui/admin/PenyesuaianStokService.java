/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartmart.ui.admin;

/**
 *
 * @author User
 */
class PenyesuaianStokService {
    public boolean sesuaikanStok(String produk, String lot, int jumlah, String tipe, String alasan) {
        try {
            System.out.println("Memproses penyesuaian stok: " + produk + ", Tipe: " + tipe + ", Jumlah: " + jumlah);
            
            return true; // Mengembalikan true jika berhasil
        } catch (Exception e) {
            return false;
        }
    }
}

